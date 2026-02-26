package mchorse.mappet.client.gui.panels;

import mchorse.mappet.Mappet;
import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.scripts.*;
import mchorse.mappet.client.gui.scripts.style.SyntaxStyle;
import mchorse.mappet.client.gui.scripts.utils.GuiItemStackOverlayPanel;
import mchorse.mappet.client.gui.scripts.utils.GuiMorphOverlayPanel;
import mchorse.mappet.client.gui.scripts.utils.GuiScriptSoundOverlayPanel;
import mchorse.mappet.client.gui.scripts.utils.documentation.DocMethod;
import mchorse.mappet.client.gui.utils.Beautifier;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlayPanel;
import mchorse.mappet.client.gui.utils.overlays.GuiSoundOverlayPanel;
import mchorse.mappet.client.gui.utils.text.undo.TextEditUndo;
import mchorse.mappet.utils.MPIcons;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.input.color.GuiColorPicker;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.utils.Color;
import mchorse.mclib.utils.Direction;
import mchorse.mclib.utils.RayTracing;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.util.MMIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Keyboard;

import javax.script.ScriptException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiScriptPanel extends GuiMappetDashboardPanel<Script> {
    public GuiIconElement toggleRepl;
    public GuiIconElement docs;
    public GuiIconElement libraries;
    public GuiIconElement run;
    public GuiIconElement beautifier;
    public GuiCodeEditor code;
    public GuiRepl repl;
    public GuiToggleElement unique;
    public GuiToggleElement globalLibrary;
    public GuiTabsLineElement tabs;

    private final Map<String, Integer> lastScrolls = new HashMap<>();

    public GuiScriptPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        folderList.setFileIcon(MMIcons.PROPERTIES);

        toggleRepl = new GuiIconElement(mc, MPIcons.get(MPIcons.CONSOLE), (b) -> setRepl(!repl.isVisible()));
        toggleRepl.tooltip(IKey.lang("mappet.gui.scripts.repl.title"), Direction.RIGHT);

        docs = new GuiIconElement(mc, Icons.HELP, this::openDocumentation);
        docs.tooltip(IKey.lang("mappet.gui.scripts.documentation.title"), Direction.RIGHT);

        libraries = new GuiIconElement(mc, Icons.MORE, this::openLibraries);
        libraries.tooltip(IKey.lang("mappet.gui.scripts.libraries.tooltip"), Direction.RIGHT);

        run = new GuiIconElement(mc, Icons.PLAY, this::runScript);
        run.tooltip(IKey.lang("mappet.gui.scripts.run"), Direction.RIGHT);

        beautifier = new GuiIconElement(mc, MPIcons.get(MPIcons.BRUSH), (b) -> beautifierScript(code));
        beautifier.tooltip(IKey.lang("mappet.gui.scripts.beautifier"), Direction.RIGHT);

        iconBar.add(toggleRepl, docs, libraries, run, beautifier);

        code = new GuiCodeEditor(mc, null);
        code.withHints();
        code.background().context(() -> createScriptContextMenu(this.mc, code));
        code.keys()
                .ignoreFocus()
                .register(IKey.lang("mappet.gui.scripts.keys.word_wrap"), Keyboard.KEY_P, this::toggleWordWrap)
                .category(GuiMappetDashboardPanel.KEYS_CATEGORY)
                .held(Keyboard.KEY_LCONTROL);

        tabs = new GuiTabsLineElement(mc, this::selectTab, this::closeTab);

        repl = new GuiRepl(mc);

        unique = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.meta.unique"), (b) -> data.unique = b.isToggled());
        globalLibrary = new GuiToggleElement(mc, IKey.lang("mappet.gui.scripts.global_library"), (b) -> data.globalLibrary = b.isToggled());

        GuiElement sideBarToggles = Elements.column(mc, 2, unique, globalLibrary);
        sideBarToggles.flex().relative(sidebar).x(10).y(1F, -10).w(1F, -20).anchorY(1F);

        folderSearch.flex().hTo(sideBarToggles.area, -5);

        tabs.flex().relative(editor).w(1F);
        code.flex().relative(editor).w(1F).h(1F, -tabs.flex().getH()).y(tabs.flex().getH());
        repl.flex().relative(editor).wh(1F, 1F);

        editor.add(tabs);
        editor.add(code);
        sidebar.prepend(sideBarToggles);
        add(repl);

        fill(null);
    }

    public static GuiContextMenu createScriptContextMenu(Minecraft mc, GuiCodeEditor editor) {
        GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc).action(Icons.BLOCK,
                        IKey.lang("mappet.gui.scripts.context.paste_block_pos"), () -> pasteBlockPosition(editor))
                .action(Icons.POSE, IKey.lang("mappet.gui.scripts.context.paste_player_pos"), () -> pastePlayerPosition(editor))
                .action(Icons.REVERSE, IKey.lang("mappet.gui.scripts.context.paste_player_rot"), () -> pastePlayerRotation(editor))
                .action(Icons.WRENCH, IKey.lang("mappet.gui.scripts.context.paste_item"), () -> openItemPicker(editor))
                .action(Icons.SOUND, IKey.lang("mappet.gui.scripts.context.paste_sound"), () -> openSoundPicker(editor))
                .action(Icons.VISIBLE, IKey.lang("mappet.gui.scripts.context.paste_morph"), () -> openMorphPicker(editor))
                .action(Icons.STOP, IKey.lang("mappet.gui.scripts.context.paste_colorRGB"), () -> openColorPicker(editor, false))
                .action(Icons.MATERIAL, IKey.lang("mappet.gui.scripts.context.paste_colorARGB"), () -> openColorPicker(editor, true));

        if (editor.isSelected()) setupDocumentation(editor, menu);

        return menu;
    }

    private static void setupDocumentation(GuiCodeEditor editor, GuiSimpleContextMenu menu) {
        String text = editor.getSelectedText().replaceAll("[^\\w_]+", "");
        List<DocMethod> searched = GuiDocumentationOverlayPanel.searchMethod(text);

        if (searched.isEmpty()) return;

        for (DocMethod docMethod : searched) {
            menu.action(Icons.SEARCH, IKey.format("mappet.gui.scripts.context.docs", docMethod.parent.getName()),
                    () -> searchDocumentation(docMethod));
        }
    }

    private static void searchDocumentation(DocMethod method) {
        GuiDocumentationOverlayPanel panel = new GuiDocumentationOverlayPanel(Minecraft.getMinecraft(), method);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), panel, 0.9F, 0.9F);
    }

    private static void pastePlayerPosition(GuiCodeEditor editor) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        DecimalFormat format = GuiTrackpadElement.FORMAT;

        editor.pasteText(format.format(player.posX) + ", " + format.format(player.posY) + ", " + format.format(player.posZ));
    }

    private static void pastePlayerRotation(GuiCodeEditor editor) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        DecimalFormat format = GuiTrackpadElement.FORMAT;

        editor.pasteText(format.format(player.rotationPitch) + ",  " + format.format(player.rotationYaw) + ", " + format.format(
                player.getRotationYawHead()));
    }

    private static void pasteBlockPosition(GuiCodeEditor editor) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        DecimalFormat format = GuiTrackpadElement.FORMAT;
        RayTraceResult result = RayTracing.rayTrace(player, 128, 0F);

        BlockPos pos = result == null || result.typeOfHit != RayTraceResult.Type.BLOCK ? player.getPosition() : result.getBlockPos();

        editor.pasteText(format.format(pos.getX()) + ", " + format.format(pos.getY()) + ", " + format.format(pos.getZ()));
    }

    private static void openItemPicker(GuiCodeEditor editor) {
        ItemStack stack = ItemStack.EMPTY;
        NBTTagCompound tag = readFromSelected(editor);

        if (tag != null) stack = new ItemStack(tag);

        GuiOverlay.addOverlay(GuiBase.getCurrent(),
                new GuiItemStackOverlayPanel(Minecraft.getMinecraft(), IKey.lang("mappet.gui.scripts.overlay.title_item"), editor, stack),
                240, 54);
    }

    private static void openMorphPicker(GuiCodeEditor editor) {
        AbstractMorph morph = null;
        NBTTagCompound tag = readFromSelected(editor);

        if (editor.isSelected()) morph = MorphManager.INSTANCE.morphFromNBT(tag);

        GuiOverlay.addOverlay(GuiBase.getCurrent(),
                new GuiMorphOverlayPanel(Minecraft.getMinecraft(), IKey.lang("mappet.gui.scripts.overlay.title_morph"), editor, morph), 240,
                54);
    }

    private static void openSoundPicker(GuiCodeEditor editor) {
        GuiSoundOverlayPanel panel = new GuiScriptSoundOverlayPanel(Minecraft.getMinecraft(), editor);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), panel, 0.5F, 0.9F);
    }

    private static void openColorPicker(GuiCodeEditor editor, boolean isArgb) {
        ValueInt valueInt = isArgb ? new ValueInt("color_picker", 0).colorAlpha() : new ValueInt("color_picker", 0).color();

        GuiOverlayPanel panel = new GuiOverlayPanel(Minecraft.getMinecraft(),
                IKey.lang("mappet.gui.scripts.context.paste_color" + (isArgb ? "A" : "") + "RGB")) {
            @Override
            public void onClose() {
                super.onClose();
                editor.pasteText(new Color(valueInt.get(), isArgb).stringify(isArgb).replaceAll("#", "0x"));
            }
        };

        GuiColorPicker picker = new GuiColorPicker(Minecraft.getMinecraft(), valueInt::set);

        if (isArgb) picker.editAlpha();

        picker.markIgnored().flex().relative(panel.content).xy(0.5f, 0.5f).anchor(0.5f, 0.5f).wh(200, 85).bounds(panel.content, 2);

        panel.content.add(picker);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), panel, 200, 160);
    }

    private static NBTTagCompound readFromSelected(GuiCodeEditor editor) {
        if (!editor.isSelected()) return null;

        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson("{String:" + editor.getSelectedText() + "}");
            return JsonToNBT.getTagFromJson(tag.getString("String"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void toggleWordWrap() {
        code.wrap();
        code.recalculate();
        code.horizontal.clamp();
        code.vertical.clamp();
    }

    private void openDocumentation(GuiIconElement element) {
        GuiDocumentationOverlayPanel panel = new GuiDocumentationOverlayPanel(mc);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), panel, 0.9F, 0.9F);
    }

    private void openLibraries(GuiIconElement element) {
        GuiLibrariesOverlayPanel overlay = new GuiLibrariesOverlayPanel(mc, data);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay, 0.4F, 0.6F);
    }

    private void runScript(GuiIconElement element) {
        EntityPlayerSP player = mc.player;

        save();
        save = false;

        player.sendChatMessage("/mp script exec " + player.getUniqueID() + " " + data.getId());
    }

    private void beautifierScript(GuiCodeEditor code) {
        String formattedCode = "";

        try {
            formattedCode = Beautifier.beautify(code.getText());
        } catch (ScriptException | NoSuchMethodException e) {
            Mappet.loggerClient.error(e.getMessage());
        }

        if (formattedCode.isEmpty()) return;

        code.selectAll();
        TextEditUndo undo = new TextEditUndo(code);
        code.deleteSelection();

        code.writeString(formattedCode);
        undo.ready().post(code.getText(), code.cursor, code.selection);
        code.getUndo().pushUndo(undo);
    }

    private void setRepl(boolean showRepl) {
        repl.setVisible(showRepl);
        code.setVisible(!showRepl);
        updateButtons();
    }

    @Override
    public boolean keyTyped(GuiContext context) {
        if (super.keyTyped(context)) return true;
        boolean ctrl = GuiScreen.isCtrlKeyDown();
        if (context.keyCode == Keyboard.KEY_S && ctrl) save();
        else if (context.keyCode == Keyboard.KEY_TAB && ctrl) {
            if (GuiScreen.isShiftKeyDown()) tabs.prevTab();
            else tabs.nextTab();
        }
        else if (code.isFocused() && data != null) tabs.edited(data.getId(), true);
        return false;
    }

    private void selectTab(String path) {
        pickData(path);
        folderList.setCurrentFile(path);
    }

    private void closeTab(String path, boolean wasSelected) {
        if (tabs.length() != 0) return;
        fill(null);
        folderList.clearCurrentFile();
    }

    private void updateButtons() {
        run.setVisible(data != null && allowed && code.isVisible());
        libraries.setVisible(data != null && allowed && code.isVisible());
        beautifier.setVisible(data != null && allowed && code.isVisible());
    }

    @Override
    protected void addNewData(String name, Script data) {
        super.addNewData(name.endsWith(".js") ? name : name + ".js", data);
    }

    @Override
    protected void dupeData(String name) {
        super.dupeData(name.endsWith(".js") ? name : name + ".js");
    }

    @Override
    public IContentType<Script> getType() {return ContentTypes.SCRIPT;}

    @Override
    public String getTitle() {return "mappet.gui.panels.scripts";}

    @Override
    protected void fillDefaultData(Script data) {
        super.fillDefaultData(data);
        data.code = MappetConfig.scriptCodeTemplate.get();
    }

    @Override
    public void fill(Script data, boolean allowed) {
        String last = this.data == null ? null : this.data.getId();

        super.fill(data, allowed);

        editor.setVisible(data != null);
        unique.setVisible(data != null && allowed);
        globalLibrary.setVisible(data != null && allowed);

        updateButtons();

        if (data == null) return;

        tabs.addTab(data.getId(), true);

        updateStyle();

        if (!code.getText().equals(data.code)) {
            if (last != null) lastScrolls.put(last, code.vertical.scroll);

            code.setText(data.code);
            setRepl(false);

            if (last != null) {
                Integer scroll = lastScrolls.get(data.getId());
                if (scroll != null) code.vertical.scroll = scroll;
            }
        }

        unique.toggled(data.unique);
        globalLibrary.toggled(data.globalLibrary);
    }

    @Override
    protected void preSave() {
        data.code = code.getText();
        tabs.edited(data.getId(), false);
    }

    @Override
    public void open() {
        super.open();
        updateStyle();
    }

    public void updateStyle() {
        SyntaxStyle style = MappetConfig.scriptEditorSyntaxStyle.get();

        if (code.getHighlighter().getStyle() == style) return;

        code.getHighlighter().setStyle(style);
        code.resetHighlight();
        repl.repl.getHighlighter().setStyle(style);
        repl.repl.resetHighlight();
    }

    public Script getData() {return data;}
}
