package mchorse.mappet.client.gui.nodes;

import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.utils.factory.IFactory;
import mchorse.mappet.api.utils.nodes.Node;
import mchorse.mappet.api.utils.nodes.NodeRelation;
import mchorse.mappet.api.utils.nodes.NodeSystem;
import mchorse.mappet.api.utils.nodes.NodeUtils;
import mchorse.mappet.utils.Colors;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.utils.GuiCanvas;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.Keybind;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Color;
import mchorse.mclib.utils.ColorUtils;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2d;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiNodeGraph<T extends Node> extends GuiCanvas {
    public static final IKey KEYS_CATEGORY = IKey.lang("mappet.gui.nodes.keys.editor");
    public static final IKey ADD_CATEGORY = IKey.lang("mappet.gui.nodes.keys.add");

    public NodeSystem<T> system;

    private final List<T> selected = new ArrayList<>();
    private boolean lastSelected;
    private boolean selecting;
    private int lastNodeX;
    private int lastNodeY;

    private T output;
    private T input;

    private final Color a = new Color();
    private final Color b = new Color();

    private boolean notifyAboutMain;
    private long tick;
    private int average;
    private int prevAverage;

    private final Consumer<T> callback;

    public GuiNodeGraph(Minecraft mc, IFactory<T> factory, Consumer<T> callback) {
        super(mc);

        this.callback = callback;

        context(() -> {
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(this.mc);

            int x = (int) fromX(GuiBase.getCurrent().mouseX);
            int y = (int) fromY(GuiBase.getCurrent().mouseY);

            menu.action(Icons.ADD, IKey.lang("mappet.gui.nodes.context.add"), () -> {
                GuiSimpleContextMenu adds = new GuiSimpleContextMenu(this.mc);

                for (String key : system.getFactory().getKeys()) {
                    IKey label = IKey.format("mappet.gui.nodes.context.add_node", IKey.lang("mappet.gui.node_types." + key));
                    int color = system.getFactory().getColor(key);

                    adds.action(Icons.ADD, label, () -> addNode(key, x, y), color);
                }

                GuiBase.getCurrent().replaceContextMenu(adds);
            });

            if (!selected.isEmpty()) {
                menu.action(Icons.COPY, IKey.lang("mappet.gui.nodes.context.copy"), this::copyNodes);
            }

            try {
                addPaste(menu, x, y);
            } catch (Exception ignored1) {
            }

            if (!selected.isEmpty()) {
                menu.action(Icons.DOWNLOAD, IKey.lang("mappet.gui.nodes.context.main"), this::markMain);
                menu.action(Icons.REVERSE, IKey.lang("mappet.gui.nodes.context.sort"), this::sortInputs);
                menu.action(Icons.MINIMIZE, IKey.lang("mappet.gui.nodes.context.tie"), this::tieSelected);
                menu.action(Icons.MAXIMIZE, IKey.lang("mappet.gui.nodes.context.untie"), this::untieSelected);
                menu.action(Icons.REMOVE, IKey.lang("mappet.gui.nodes.context.remove"), this::removeSelected, Colors.NEGATIVE);
            }

            return menu;
        });

        keys().register(IKey.lang("mappet.gui.nodes.context.tie"), Keyboard.KEY_F, this::tieSelected).inside().category(KEYS_CATEGORY);
        keys()
                .register(IKey.lang("mappet.gui.nodes.context.untie"), Keyboard.KEY_U, this::untieSelected)
                .inside()
                .category(KEYS_CATEGORY);
        keys().register(IKey.lang("mappet.gui.nodes.context.main"), Keyboard.KEY_M, this::markMain).inside().category(KEYS_CATEGORY);
        keys().register(IKey.lang("mappet.gui.nodes.context.sort"), Keyboard.KEY_C, this::sortInputs).inside().category(KEYS_CATEGORY);

        int keycode = Keyboard.KEY_1;

        for (String key : factory.getKeys()) {
            Keybind keybind = keys()
                    .register(IKey.format("mappet.gui.nodes.context.add_node", IKey.lang("mappet.gui.node_types." + key)), keycode, () -> {
                        GuiContext context = GuiBase.getCurrent();

                        addNode(key, (int) fromX(context.mouseX), (int) fromY(context.mouseY));
                    });

            keybind.inside().held(Keyboard.KEY_LCONTROL).category(ADD_CATEGORY);
            keycode += 1;
        }
    }

    public GuiNodeGraph<T> notifyAboutMain() {
        notifyAboutMain = true;

        return this;
    }

    /* Copy/paste */

    private void copyNodes() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        NBTTagCompound relations = new NBTTagCompound();

        for (T node : selected) {
            NBTTagCompound nodeTag = NodeUtils.nodeToNBT(system, node);

            list.appendTag(nodeTag);

            List<T> children = system.getChildren(node);

            for (T child : children) {
                if (selected.contains(child)) {
                    NBTTagList relation;
                    String key = node.getId().toString();

                    if (relations.hasKey(key)) {
                        relation = relations.getTagList(key, Constants.NBT.TAG_STRING);
                    }
                    else {
                        relation = new NBTTagList();
                        relations.setTag(key, relation);
                    }

                    relation.appendTag(new NBTTagString(child.getId().toString()));
                }
            }
        }

        tag.setBoolean("_CopyNodes", true);
        tag.setTag("Nodes", list);
        tag.setTag("Relations", relations);
        GuiScreen.setClipboardString(tag.toString());
    }

    private void addPaste(GuiSimpleContextMenu menu, int x, int y) throws NBTException {
        String json = GuiScreen.getClipboardString();

        NBTTagCompound tag = JsonToNBT.getTagFromJson(json);

        if (!tag.getBoolean("_CopyNodes")) {
            return;
        }

        NBTTagList nodesTag = tag.getTagList("Nodes", Constants.NBT.TAG_COMPOUND);
        NBTTagCompound relationsTag = tag.getCompoundTag("Relations");

        List<T> nodes = new ArrayList<>();
        Map<String, T> mapping = new HashMap<>();

        for (int i = 0; i < nodesTag.tagCount(); i++) {
            NBTTagCompound nodeTag = nodesTag.getCompoundTagAt(i);
            String id = nodeTag.getString("Id");

            nodeTag.removeTag("Id");

            T node = NodeUtils.nodeFromNBT(system, nodeTag);

            mapping.put(id, node);
            nodes.add(node);
        }

        int nx = nodes.get(0).x;
        int ny = nodes.get(0).y;

        menu.action(Icons.PASTE, IKey.lang("mappet.gui.nodes.context.paste"), () -> {
            selected.clear();

            for (T node : nodes) {
                system.add(node);

                node.x = node.x - nx + x;
                node.y = node.y - ny + y;

                select(node, true);
            }

            for (String key : relationsTag.getKeySet()) {
                NBTTagList relations = relationsTag.getTagList(key, Constants.NBT.TAG_STRING);
                T output = mapping.get(key);

                for (NBTBase base : relations) {
                    T input = mapping.get(((NBTTagString) base).getString());

                    if (output != null && input != null) {
                        system.tie(output, input);
                    }
                }
            }
        });
    }

    /* CRUD */

    private void addNode(String key, int x, int y) {
        T node = system.getFactory().create(key);

        if (node != null) {
            node.x = x;
            node.y = y;

            system.add(node);
            select(node);
        }
    }

    private void removeSelected() {
        for (T selected : selected) {
            system.remove(selected);
        }

        if (system.main != null && selected.contains(system.main)) {
            system.main = null;
        }

        select(null);
    }

    private void tieSelected() {
        if (selected.size() <= 1) {
            return;
        }

        T last = selected.get(selected.size() - 1);
        List<T> nodes = new ArrayList<>(selected);

        nodes.remove(last);
        nodes.sort(Comparator.comparingInt(a -> a.x));

        for (T node : nodes) {
            system.tie(last, node);
        }
    }

    private void untieSelected() {
        if (selected.isEmpty()) {
            return;
        }

        if (selected.size() == 1) {
            system.relations.remove(selected.get(0).getId());
        }
        else if (selected.size() == 2) {
            /* Untying from both sides */
            T a = selected.get(0);
            T b = selected.get(1);

            system.untie(a, b);
            system.untie(b, a);
        }
        else {
            T last = selected.get(selected.size() - 1);

            for (int i = 0; i < selected.size() - 1; i++) {
                system.untie(last, selected.get(i));
            }
        }
    }

    private void markMain() {
        if (selected.isEmpty()) {
            return;
        }

        system.main = selected.get(selected.size() - 1);
    }

    private void sortInputs() {
        if (selected.size() != 1) {
            return;
        }

        T node = selected.get(0);
        List<NodeRelation<T>> relations = system.relations.get(node.getId());

        if (relations != null) {
            relations.sort(Comparator.comparingInt(a -> a.input.x));
        }
    }

    public void setNode(T node) {
        if (callback != null) {
            callback.accept(node);
        }
    }

    public void select(T node) {
        select(node, false);
    }

    public void select(T node, boolean add) {
        if (!add) {
            selected.clear();
        }

        if (node != null) {
            selected.add(node);
        }

        setNode(node);
    }

    public Area getNodeArea(T node) {
        int x1 = toX(node.x - 60);
        int y1 = toY(node.y - 35);
        int x2 = toX(node.x + 60);
        int y2 = toY(node.y + 35);

        Area.SHARED.setPoints(x1, y1, x2, y2);

        return Area.SHARED;
    }

    public Area getNodeOutletArea(Area nodeArea, boolean output) {
        int y = output ? 7 : -7;

        int x1 = nodeArea.mx() - 4;
        int y1 = nodeArea.y(output ? 1F : 0F) - 4 + y;
        int x2 = nodeArea.mx() + 4;
        int y2 = nodeArea.y(output ? 1F : 0F) + 4 + y;

        Area area = new Area();

        area.setPoints(x1, y1, x2, y2);

        return area;
    }

    public boolean isConnecting() {
        return output != null || input != null;
    }

    public void set(NodeSystem<T> system) {
        boolean same = this.system != null && system != null && this.system.getId().equals(system.getId());

        this.system = system;

        if (system != null && !same) {
            int x = system.main == null ? 0 : system.main.x;
            int y = system.main == null ? 0 : system.main.y;

            if (system.main == null && !system.nodes.isEmpty()) {
                for (T node : system.nodes.values()) {
                    x += node.x;
                    y += node.y;
                }

                x /= system.nodes.size();
                y /= system.nodes.size();
            }

            scaleX.setShift(x);
            scaleY.setShift(y);
            scaleX.setZoom(0.5F);
            scaleY.setZoom(0.5F);
        }

        if (same) {
            List<UUID> ids = selected.stream().map(Node::getId).collect(Collectors.toList());

            selected.clear();

            for (UUID uuid : ids) {
                selected.add(this.system.nodes.get(uuid));
            }

            setNode(selected.isEmpty() ? null : selected.get(selected.size() - 1));
        }
        else {
            selected.clear();
        }
    }

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (super.mouseClicked(context) && context.mouseButton == 2) {
            return true;
        }

        if (system == null) {
            return false;
        }

        if (context.mouseButton == 0) {
            lastNodeX = (int) fromX(context.mouseX);
            lastNodeY = (int) fromY(context.mouseY);
            boolean shift = GuiScreen.isShiftKeyDown();
            List<T> nodes = new ArrayList<>(system.nodes.values());

            Collections.reverse(nodes);

            for (T node : nodes) {
                Area nodeArea = getNodeArea(node);

                if (nodeArea.isInside(context)) {
                    if (shift) {
                        if (!selected.contains(node)) {
                            select(node, true);
                        }
                        else {
                            selected.remove(node);
                            select(node, true);
                        }
                    }
                    else if (!selected.contains(node)) {
                        select(node);
                    }

                    lastSelected = true;

                    return true;
                }
                Area output = getNodeOutletArea(nodeArea, true);
                Area input = getNodeOutletArea(nodeArea, false);

                if (output.isInside(context)) {
                    this.output = node;
                }
                else if (input.isInside(context) && system.main != node) {
                    this.input = node;
                }

                if (isConnecting()) {
                    return false;
                }
            }

            if (shift) {
                selecting = true;
            }
            else {
                select(null);
            }
        }

        return false;
    }

    @Override
    protected void startDragging(GuiContext context) {
        /* Fake middle mouse click to add an ability to navigate
         * with Ctrl + click dragging */
        if (context.mouseButton == 0 && GuiScreen.isCtrlKeyDown()) {
            mouse = 2;
        }

        super.startDragging(context);
    }

    @Override
    public void mouseReleased(GuiContext context) {
        super.mouseReleased(context);

        if (isConnecting()) {
            boolean output = this.output != null;

            for (T node : system.nodes.values()) {
                Area nodeArea = getNodeArea(node);
                Area outlet = getNodeOutletArea(nodeArea, !output);

                if (outlet.isInside(context)) {
                    if (output) {
                        input = node;
                    }
                    else {
                        this.output = node;
                    }

                    break;
                }
            }
        }

        if (selecting) {
            Area area = new Area();
            boolean wasSelected = !selected.isEmpty();

            area.setPoints(lastX, lastY, context.mouseX, context.mouseY);

            for (T node : system.nodes.values()) {
                Area nodeArea = getNodeArea(node);

                if (nodeArea.intersects(area) && !selected.contains(node)) {
                    selected.add(0, node);
                }
            }

            if (!wasSelected && !selected.isEmpty()) {
                setNode(selected.get(selected.size() - 1));
            }
        }
        else if (output != null && input != null && input != output) {
            system.tie(output, input);
        }

        lastSelected = false;
        selecting = false;
        output = input = null;
    }

    @Override
    protected void dragging(GuiContext context) {
        super.dragging(context);

        if (dragging && mouse == 0 && lastSelected && !selected.isEmpty()) {
            int lastNodeX = (int) fromX(context.mouseX);
            int lastNodeY = (int) fromY(context.mouseY);

            for (T node : selected) {
                node.x += lastNodeX - this.lastNodeX;
                node.y += lastNodeY - this.lastNodeY;
            }

            this.lastNodeX = lastNodeX;
            this.lastNodeY = lastNodeY;
        }
    }

    @Override
    public void draw(GuiContext context) {
        if (area.isInside(context) && !context.isFocused()) {
            float steps = prevAverage <= 0 ? 1 : prevAverage;
            float step = 15 / steps;
            float x = Keyboard.isKeyDown(Keyboard.KEY_LEFT) ? -step : Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ? step : 0;
            float y = Keyboard.isKeyDown(Keyboard.KEY_UP) ? -step : Keyboard.isKeyDown(Keyboard.KEY_DOWN) ? step : 0;

            if (x != 0) {
                scaleX.setShift(x / scaleX.getZoom() + scaleX.getShift());
            }

            if (y != 0) {
                scaleY.setShift(y / scaleY.getZoom() + scaleY.getShift());
            }

            /* Limiting speed so it wouldn't go crazy fast for people who play on
             * absurd frame rates (like 300 or something like that) */
            average += 1;

            if (tick < context.tick) {
                tick = context.tick;
                prevAverage = average;
                average = 0;
            }
        }

        super.draw(context);

        if (system.nodes.isEmpty()) {
            int w = area.w / 2;

            GlStateManager.enableTexture2D();
            GuiDraw.drawMultiText(font, I18n.format("mappet.gui.nodes.info.empty_nodes"), area.mx(w), area.my(), 0xffffff, w, 12, 0.5F, 0.5F);
        }
        else if (notifyAboutMain && system.main == null) {
            String label = I18n.format("mappet.gui.nodes.info.empty_main");
            int w = font.getStringWidth(label);

            Gui.drawRect(area.x + 4, area.y + 4, area.x + 24 + w, area.y + 20, ColorUtils.HALF_BLACK);
            GlStateManager.color(1F, 0F, 0.1F, 1F);
            Icons.EXCLAMATION.render(area.x + 4, area.y + 4);
            font.drawStringWithShadow(label, area.x + 20, area.y + 8, 0xff0010);
        }
    }

    @Override
    protected void drawCanvas(GuiContext context) {
        super.drawCanvas(context);

        if (system == null) {
            return;
        }

        int thickness = MappetConfig.nodeThickness.get();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.glLineWidth(thickness);

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        T lastSelected = selected.isEmpty() ? null : selected.get(selected.size() - 1);
        List<Vector2d> positions = new ArrayList<>();

        /* Draw connections */
        if (thickness > 0) {
            builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            renderConnections(context, builder, positions, lastSelected);

            Tessellator.getInstance().draw();
        }

        /* Draw node boxes */
        Area main = null;

        for (T node : system.nodes.values()) {
            Area nodeArea = getNodeArea(node);

            if (nodeArea.w > 25) {
                renderOutlets(context, node, nodeArea);
            }

            boolean hover = Area.SHARED.isInside(context);
            int index = selected.indexOf(node);

            int colorBg = hover ? 0xff080808 : 0xff000000;
            int colorFg = 0xaa000000 + system.getFactory().getColor(node);

            if (index >= 0) {
                int colorSh = index == selected.size() - 1 ? 0x0088ff : 0x0022aa;

                GuiDraw.drawDropShadow(nodeArea.x + 4, nodeArea.y + 4, nodeArea.ex() - 4, nodeArea.ey() - 4, 8, 0xff000000 + colorSh, colorSh);
            }

            Gui.drawRect(nodeArea.x + 1, nodeArea.y, nodeArea.ex() - 1, nodeArea.ey(), colorBg);
            Gui.drawRect(nodeArea.x, nodeArea.y + 1, nodeArea.ex(), nodeArea.ey() - 1, colorBg);
            GuiDraw.drawOutline(nodeArea.x + 3, nodeArea.y + 3, nodeArea.ex() - 3, nodeArea.ey() - 3, colorFg);

            if (node == system.main) {
                main = new Area();
                main.copy(nodeArea);
            }
        }

        for (T node : system.nodes.values()) {
            Area nodeArea = getNodeArea(node);
            String title = node.getTitle();

            if (!title.isEmpty() && nodeArea.w > 40) {
                if (title.length() > 37) {
                    title = title.substring(0, 37) + "§r...";
                }

                GuiDraw.drawTextBackground(font, title, nodeArea.mx() - font.getStringWidth(title) / 2, nodeArea.my() - 4, 0xffffff, ColorUtils.HALF_BLACK);
            }
        }

        /* Draw selected node's indices */
        for (int i = 0; i < positions.size(); i++) {
            Vector2d pos = positions.get(i);
            String label = String.valueOf(i);

            font.drawStringWithShadow(label, (int) pos.x - (float) font.getStringWidth(label) / 2, (int) pos.y - 4, getIndexLabelColor(lastSelected, i));
        }

        /* Draw main entry node icon */
        if (main != null) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            GuiDraw.drawOutlinedIcon(Icons.DOWNLOAD, main.mx(), main.y - 4, 0xffffffff, 0.5F, 1F);
        }

        GlStateManager.glLineWidth(1);

        /* Draw selection */
        if (selecting) {
            Gui.drawRect(lastX, lastY, context.mouseX, context.mouseY, 0x440088ff);
        }
    }

    private void renderOutlets(GuiContext context, T node, Area nodeArea) {
        Area output = getNodeOutletArea(nodeArea, true);
        Area input = getNodeOutletArea(nodeArea, false);

        boolean insideO = output.isInside(context);
        boolean insideI = input.isInside(context);

        int colorO = ColorUtils.multiplyColor(0xffffff, insideO ? 1F : 0.6F);
        int colorI = ColorUtils.multiplyColor(0xffffff, insideI ? 1F : 0.6F);

        if (this.output == node) {
            colorO = Colors.ACTIVE;

            if (insideI) {
                colorI = Colors.NEGATIVE;
            }
        }
        else if (this.output != null) {
            if (insideO) {
                colorO = Colors.NEGATIVE;
            }
            else if (insideI) {
                colorI = Colors.POSITIVE;
            }
        }

        if (this.input == node) {
            colorI = Colors.ACTIVE;

            if (insideO) {
                colorO = Colors.NEGATIVE;
            }
        }
        else if (this.input != null) {
            if (insideI) {
                colorI = Colors.NEGATIVE;
            }
            else if (insideO) {
                colorO = Colors.POSITIVE;
            }
        }

        GuiDraw.drawOutline(output.x, output.y, output.ex(), output.ey(), 0xff000000 + colorO);

        if (system.main != node) {
            GuiDraw.drawOutline(input.x, input.y, input.ex(), input.ey(), 0xff000000 + colorI);
        }
    }

    private void renderConnections(GuiContext context, BufferBuilder builder, List<Vector2d> positions, T lastSelected) {
        for (List<NodeRelation<T>> relations : system.relations.values()) {
            for (int r = 0; r < relations.size(); r++) {
                NodeRelation<T> relation = relations.get(r);

                Area output = getNodeOutletArea(getNodeArea(relation.output), true);
                Area input = getNodeOutletArea(getNodeArea(relation.input), false);

                int x1 = input.mx();
                int y1 = input.my();
                int x2 = output.mx();
                int y2 = output.my();

                drawConnection(builder, context, relation.output, r, x1, y1, x2, y2, false);

                if (relation.output == lastSelected) {
                    positions.add(new Vector2d((x1 + x2) / 2F, (y1 + y2) / 2F));
                }
            }
        }

        if (isConnecting()) {
            T node = output == null ? input : output;
            Area area = getNodeArea(node);
            Area outlet = getNodeOutletArea(area, node == output);

            int x1 = context.mouseX;
            int y1 = context.mouseY;
            int x2 = outlet.mx();
            int y2 = outlet.my();

            List<NodeRelation<T>> list = system.relations.get(node.getId());

            drawConnection(builder, context, node, list == null ? 0 : list.size(), x1, y1, x2, y2, true);
        }
    }

    /**
     * Draw the connection line
     */
    private void drawConnection(BufferBuilder builder, GuiContext context, T node, int r, int x1, int y1, int x2, int y2, boolean forceLine) {
        float factor = (context.tick + context.partialTicks) / 60F;
        final float segments = 8F;

        float opacity = getNodeActiveColorOpacity(node, r);
        int c1 = MappetConfig.nodePulseBackgroundMcLibPrimary.get() ? McLib.primaryColor.get() : MappetConfig.nodePulseBackgroundColor.get();
        int c2 = getNodeActiveColor(node, r);

        for (int i = 0; i < segments; i++) {
            float factor1 = i / segments;
            float factor2 = (i + 1) / segments;
            float color1 = 1 - MathUtils.clamp(Math.abs(1 - factor1 - factor % 1) / 0.2F, 0F, 1F);
            float color2 = 1 - MathUtils.clamp(Math.abs(1 - factor2 - factor % 1) / 0.2F, 0F, 1F);

            color1 = Math.max(color1, 1 - MathUtils.clamp(Math.abs(1 - factor1 + 1 - factor % 1) / 0.2F, 0F, 1F));
            color2 = Math.max(color2, 1 - MathUtils.clamp(Math.abs(1 - factor2 + 1 - factor % 1) / 0.2F, 0F, 1F));

            color1 = Math.max(color1, 1 - MathUtils.clamp(Math.abs(1 - factor1 - 1 - factor % 1) / 0.2F, 0F, 1F));
            color2 = Math.max(color2, 1 - MathUtils.clamp(Math.abs(1 - factor2 - 1 - factor % 1) / 0.2F, 0F, 1F));

            ColorUtils.interpolate(a, c1, c2, color1, false);
            ColorUtils.interpolate(b, c1, c2, color2, false);

            a.a = opacity;
            b.a = opacity;

            if (y2 <= y1 || forceLine) {
                builder
                        .pos(Interpolations.lerp(x1, x2, factor1), Interpolations.lerp(y1, y2, factor1), 0)
                        .color(a.r, a.g, a.b, a.a)
                        .endVertex();
                builder
                        .pos(Interpolations.lerp(x1, x2, factor2), Interpolations.lerp(y1, y2, factor2), 0)
                        .color(b.r, b.g, b.b, b.a)
                        .endVertex();
            }
            else {
                if (i == segments / 2) {
                    builder.pos(Interpolations.lerp(x1, x2, 0.5F), y1, 0).color(a.r, a.g, a.b, a.a).endVertex();
                    builder.pos(Interpolations.lerp(x1, x2, 0.5F), y2, 0).color(b.r, b.g, b.b, b.a).endVertex();
                }
                else {
                    int y = i < segments / 2 ? y1 : y2;

                    builder
                            .pos(Interpolations.lerp(x1, x2, i == segments / 2 + 1 ? 0.5F : factor1), y, 0)
                            .color(a.r, a.g, a.b, a.a)
                            .endVertex();
                    builder
                            .pos(Interpolations.lerp(x1, x2, i == segments / 2 - 1 ? 0.5F : factor2), y, 0)
                            .color(b.r, b.g, b.b, b.a)
                            .endVertex();
                }
            }
        }
    }

    protected int getIndexLabelColor(T lastSelected, int i) {
        return 0xffffff;
    }

    protected int getNodeActiveColor(T output, int r) {
        return Colors.ACTIVE;
    }

    protected float getNodeActiveColorOpacity(T output, int r) {
        return 0.75F;
    }
}