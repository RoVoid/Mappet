package mchorse.mappet.client.gui.panels;

import mchorse.mappet.MappetFactories;
import mchorse.mappet.proxy.CommonProxy;
import mchorse.mappet.api.scripts.code.ui.MappetUIBuilder;
import mchorse.mappet.api.scripts.code.ui.UIButtonComponent;
import mchorse.mappet.api.scripts.code.ui.UIComponent;
import mchorse.mappet.api.scripts.code.ui.UIGraphicsComponent;
import mchorse.mappet.api.scripts.code.ui.UILabelBaseComponent;
import mchorse.mappet.api.scripts.code.ui.UIMorphComponent;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.graphics.GradientGraphic;
import mchorse.mappet.client.gui.utils.graphics.Graphic;
import mchorse.mappet.client.gui.utils.graphics.ImageGraphic;
import mchorse.mappet.client.gui.utils.graphics.ShadowGraphic;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.creative.GuiNestedEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuiUIConstructorPanel extends GuiMappetDashboardPanel<UI>
{
    private final Map<String, UIComponent> componentsByName = new LinkedHashMap<>();

    private GuiElement left;
    private GuiElement center;
    private GuiElement right;
    private GuiElement viewport;
    private GuiElement previewHolder;

    private GuiStringListElement components;
    private GuiScrollElement properties;
    private GuiIconElement copyCode;
    private GuiIconElement previewToggle;

    private GuiTextElement id;
    private GuiTextElement label;
    private GuiTextElement image;

    private GuiTrackpadElement x;
    private GuiTrackpadElement y;
    private GuiTrackpadElement w;
    private GuiTrackpadElement h;
    private GuiTrackpadElement anchorX;
    private GuiTrackpadElement anchorY;
    private GuiTrackpadElement shadowOffset;

    private GuiColorElement primaryColor;
    private GuiColorElement secondaryColor;
    private GuiColorElement backgroundColor;

    private GuiNestedEdit morph;

    private UIComponent selected;
    private boolean syncing;
    private int primaryColorValue = 0xffffffff;
    private int secondaryColorValue = 0x00000000;
    private int backgroundColorValue = 0xff6f5dd6;
    private boolean previewEnabled = true;

    public GuiUIConstructorPanel(Minecraft mc, GuiMappetDashboard dashboard)
    {
        super(mc, dashboard);

        this.folderList.setFileIcon(Icons.PROCESSOR);

        this.left = new GuiElement(mc);
        this.center = new GuiElement(mc);
        this.right = new GuiElement(mc);
        this.viewport = new GuiElement(mc)
        {
            @Override
            public void draw(GuiContext context)
            {
                Gui.drawRect(this.area.x, this.area.y, this.area.ex(), this.area.ey(), 0xcc151515);
                Gui.drawRect(this.area.x + 1, this.area.y + 1, this.area.ex() - 1, this.area.ey() - 1, 0xff1d1f24);
                super.draw(context);
            }
        };
        this.previewHolder = new GuiElement(mc);

        this.components = new GuiStringListElement(mc, (list) ->
        {
            if (!list.isEmpty())
            {
                this.selectComponent(this.componentsByName.get(list.get(0)));
            }
        });
        this.components.background();
        this.components.context(this::createComponentsContextMenu);

        GuiScrollElement addButtons = new GuiScrollElement(mc);
        addButtons.flex().relative(this.left).xy(8, 8).w(1F, -16).h(162).column(2).stretch().vertical().scroll().padding(0);
        addButtons.add(
            this.createActionButton("+ Button", this::addButton),
            this.createActionButton("+ Label", this::addLabel),
            this.createActionButton("+ Rect", this::addRect),
            this.createActionButton("+ Gradient", this::addGradient),
            this.createActionButton("+ Shadow", this::addShadow),
            this.createActionButton("+ Image", this::addImage),
            this.createActionButton("+ Morph", this::addMorph)
        );

        this.id = new GuiTextElement(mc, 128, (value) ->
        {
            if (this.syncing || this.selected == null) return;

            this.selected.id(value);
            this.changed();
        });
        this.label = new GuiTextElement(mc, 500, (value) ->
        {
            if (this.syncing || this.selected == null) return;

            if (this.selected instanceof UILabelBaseComponent)
            {
                ((UILabelBaseComponent) this.selected).label(value);
                this.changed();
            }
        });
        this.image = new GuiTextElement(mc, 256, (value) ->
        {
            if (this.syncing || this.selected == null) return;

            Graphic graphic = this.getPrimaryGraphic();

            if (graphic instanceof ImageGraphic)
            {
                try
                {
                    ((ImageGraphic) graphic).picture = RLUtils.create(value);
                    this.changed();
                }
                catch (Exception ignored)
                {}
            }
        });

        this.x = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.y = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.w = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.h = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.anchorX = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.anchorY = new GuiTrackpadElement(mc, (v) -> this.applyFrame());
        this.shadowOffset = new GuiTrackpadElement(mc, (v) -> this.applyStyle());
        this.shadowOffset.limit(0);

        this.primaryColor = new GuiColorElement(mc, (c) ->
        {
            this.primaryColorValue = c;
            this.applyStyle();
        });
        this.secondaryColor = new GuiColorElement(mc, (c) ->
        {
            this.secondaryColorValue = c;
            this.applyStyle();
        });
        this.backgroundColor = new GuiColorElement(mc, (c) ->
        {
            this.backgroundColorValue = c;
            this.applyStyle();
        });

        this.morph = new GuiNestedEdit(mc, this::openMorphMenu);
        this.copyCode = new GuiIconElement(mc, Icons.COPY, (b) -> this.copyGeneratedCode());
        this.copyCode.tooltip(IKey.str("Скопировать код UI"));
        this.previewToggle = new GuiIconElement(mc, Icons.POSE, (b) ->
        {
            this.previewEnabled = !this.previewEnabled;
            this.previewHolder.setVisible(this.previewEnabled);
            this.previewToggle.both(this.previewEnabled ? Icons.POSE : Icons.BLOCK);
            this.previewToggle.tooltip(IKey.str(this.previewEnabled ? "Скрыть предпросмотр" : "Показать предпросмотр"));
        });
        this.previewToggle.tooltip(IKey.str("Скрыть предпросмотр"));

        this.properties = new GuiScrollElement(mc);
        this.properties.flex().relative(this.right).y(24).w(1F).h(1F, -24).column(5).stretch().vertical().scroll().padding(8);
        this.properties.add(
            Elements.label(IKey.str("ID")),
            this.id,
            Elements.label(IKey.str("Text")),
            this.label,
            Elements.label(IKey.str("X / Y")),
            this.x,
            this.y,
            Elements.label(IKey.str("W / H")),
            this.w,
            this.h,
            Elements.label(IKey.str("Anchor X / Y")),
            this.anchorX,
            this.anchorY,
            Elements.label(IKey.str("Primary Color")),
            this.primaryColor,
            Elements.label(IKey.str("Secondary Color")),
            this.secondaryColor,
            Elements.label(IKey.str("Background Color")),
            this.backgroundColor,
            Elements.label(IKey.str("Shadow Offset")),
            this.shadowOffset,
            Elements.label(IKey.str("Image Resource")),
            this.image,
            Elements.label(IKey.str("Morph")),
            this.morph
        );

        this.left.flex().relative(this.editor).w(150).h(1F);
        this.center.flex().relative(this.editor).x(150).w(1F, -330).h(1F);
        this.right.flex().relative(this.editor).x(1F, -12).w(180).h(1F).anchorX(1F);

        this.components.flex().relative(this.left).x(8).y(176).w(1F, -16).h(1F, -184);
        this.previewToggle.flex().relative(this.right).x(1F, -28).y(6).anchorX(1F);
        this.copyCode.flex().relative(this.right).x(1F, -6).y(6).anchorX(1F);

        this.viewport.flex().relative(this.center).xy(8, 8).w(1F, -16).h(1F, -16);
        this.previewHolder.flex().relative(this.viewport).wh(1F, 1F);

        this.viewport.add(this.previewHolder);
        this.left.add(addButtons, this.components);
        this.right.add(this.properties, this.previewToggle, this.copyCode);
        this.editor.add(this.left, this.center, this.right, this.viewport);

        this.fill(null);
    }

    private void addButton()
    {
        UIButtonComponent component = new UIButtonComponent();

        component.id(this.generateId("button"));
        component.label("Button");
        component.xy(18, 18).wh(120, 20);
        component.background(0xff6f5dd6);

        this.addComponent(component);
    }

    private void addLabel()
    {
        MappetUIBuilder builder = new MappetUIBuilder(this.data, "", "");
        UILabelBaseComponent component = (UILabelBaseComponent) builder.label("Label");

        component.id(this.generateId("label"));
        component.xy(24, 24).wh(100, 20);

        this.addComponent(component);
    }

    private void addRect()
    {
        UIGraphicsComponent component = new UIGraphicsComponent();

        component.id(this.generateId("graphics"));
        component.xy(24, 24).wh(120, 80);
        component.rect(0xffa46cff);

        this.addComponent(component);
    }

    private void addGradient()
    {
        UIGraphicsComponent component = new UIGraphicsComponent();

        component.id(this.generateId("gradient"));
        component.xy(24, 24).wh(140, 80);
        component.gradient(0xfff7d04a, 0xff7ae957, true);

        this.addComponent(component);
    }

    private void addShadow()
    {
        UIGraphicsComponent component = new UIGraphicsComponent();

        component.id(this.generateId("shadow"));
        component.xy(30, 30).wh(130, 90);
        component.shadow(0x88000000, 0x00000000, 18);

        this.addComponent(component);
    }

    private void addImage()
    {
        UIGraphicsComponent component = new UIGraphicsComponent();

        component.id(this.generateId("image"));
        component.xy(18, 18).wh(160, 90);
        component.image("minecraft:textures/gui/options_background.png", 0, 0, 160, 90, 16, 16, 0xffffffff);

        this.addComponent(component);
    }

    private void addMorph()
    {
        UIMorphComponent component = new UIMorphComponent();

        component.id(this.generateId("morph"));
        component.xy(30, 30).wh(120, 120);
        component.position(0, 1, 0);
        component.rotation(0, 0);
        component.distance(2);
        component.fov(40);
        component.editing(false);

        this.addComponent(component);
    }

    private void addComponent(UIComponent component)
    {
        if (this.data == null) return;

        this.data.root.getChildComponents().add(component);
        component.tooltip(this.typeName(component) + " | " + component.id);
        this.selectComponent(component);
        this.changed();
    }

    private void removeSelected()
    {
        if (this.data == null || this.selected == null) return;

        this.data.root.getChildComponents().remove(this.selected);
        this.selectComponent(this.data.root.getChildComponents().isEmpty() ? null : this.data.root.getChildComponents().get(0));
        this.changed();
    }

    private void openMorphMenu(boolean editing)
    {
        if (!(this.selected instanceof UIMorphComponent)) return;

        UIMorphComponent morphComponent = (UIMorphComponent) this.selected;
        AbstractMorph currentMorph = MorphManager.INSTANCE.morphFromNBT(morphComponent.morph);

        GuiMappetDashboard.get(this.mc).openMorphMenu(this.getParentContainer(), editing, currentMorph, this::setMorph);
    }

    private void setMorph(AbstractMorph morph)
    {
        if (!(this.selected instanceof UIMorphComponent)) return;

        morph = MorphUtils.copy(morph);
        ((UIMorphComponent) this.selected).morph(morph);
        this.morph.setMorph(morph);
        this.changed();
    }

    private void applyFrame()
    {
        if (this.syncing || this.selected == null) return;

        this.selected.xy((int) this.x.value, (int) this.y.value);
        this.selected.wh((int) this.w.value, (int) this.h.value);
        this.selected.anchor((float) this.anchorX.value, (float) this.anchorY.value);

        this.changed();
    }

    private void applyStyle()
    {
        if (this.syncing || this.selected == null) return;

        if (this.selected instanceof UIButtonComponent)
        {
            ((UIButtonComponent) this.selected).background(this.backgroundColorValue);
        }

        if (this.selected instanceof UILabelBaseComponent)
        {
            ((UILabelBaseComponent) this.selected).color(this.primaryColorValue, true);
        }

        if (this.selected instanceof UIGraphicsComponent)
        {
            Graphic graphic = this.getPrimaryGraphic();

            if (graphic != null)
            {
                graphic.primary = this.primaryColorValue;
            }

            if (graphic instanceof GradientGraphic)
            {
                ((GradientGraphic) graphic).secondary = this.secondaryColorValue;
            }
            else if (graphic instanceof ShadowGraphic)
            {
                ((ShadowGraphic) graphic).secondary = this.secondaryColorValue;
                ((ShadowGraphic) graphic).offset = (int) this.shadowOffset.value;
            }
        }

        this.changed();
    }

    private Graphic getPrimaryGraphic()
    {
        if (!(this.selected instanceof UIGraphicsComponent)) return null;

        UIGraphicsComponent component = (UIGraphicsComponent) this.selected;

        return component.graphics.isEmpty() ? null : component.graphics.get(0);
    }

    private void changed()
    {
        this.updateTooltips();
        this.rebuildList();
        this.rebuildPreview();
        this.save();
    }

    private String generateId(String prefix)
    {
        int maxIndex = 0;

        if (this.data != null)
        {
            String expectedPrefix = prefix + "_";

            for (UIComponent component : this.data.root.getChildComponents())
            {
                if (component.id == null || !component.id.startsWith(expectedPrefix)) continue;

                String tail = component.id.substring(expectedPrefix.length());

                try
                {
                    maxIndex = Math.max(maxIndex, Integer.parseInt(tail));
                }
                catch (Exception ignored)
                {}
            }
        }

        return prefix + "_" + (maxIndex + 1);
    }

    private void updateTooltips()
    {
        if (this.data == null) return;

        for (UIComponent component : this.data.root.getChildComponents())
        {
            component.tooltip(this.typeName(component) + " | " + (component.id == null ? "" : component.id));
        }
    }

    private void selectComponent(UIComponent component)
    {
        this.selected = component;
        this.syncProperties();
        this.rebuildList();
        this.rebuildPreview();
    }

    private void rebuildList()
    {
        this.componentsByName.clear();

        List<String> names = new ArrayList<>();
        String selectedName = null;
        int index = 1;

        if (this.data != null)
        {
            for (UIComponent component : this.data.root.getChildComponents())
            {
                String id = component.id == null || component.id.isEmpty() ? this.typeName(component) + "_" + index : component.id;
                String name = this.typeName(component) + " | " + id;

                names.add(name);
                this.componentsByName.put(name, component);

                if (component == this.selected)
                {
                    selectedName = name;
                }

                index += 1;
            }
        }

        this.components.setList(names);

        if (selectedName != null)
        {
            this.components.setCurrentScroll(selectedName);
        }
    }

    private String typeName(UIComponent component)
    {
        String type = MappetFactories.getUiComponents().type(component);

        return type == null || type.isEmpty() ? component.getClass().getSimpleName() : type;
    }

    private void syncProperties()
    {
        this.syncing = true;

        boolean exists = this.selected != null;

        this.properties.setEnabled(exists);

        if (!exists)
        {
            this.id.setText("");
            this.label.setText("");
            this.image.setText("");
            this.morph.setMorph(null);
            this.syncing = false;
            return;
        }

        this.id.setText(this.selected.id == null ? "" : this.selected.id);
        this.x.setValue(this.selected.x.offset);
        this.y.setValue(this.selected.y.offset);
        this.w.setValue(this.selected.w.offset);
        this.h.setValue(this.selected.h.offset);
        this.anchorX.setValue(this.selected.x.anchor);
        this.anchorY.setValue(this.selected.y.anchor);

        NBTTagCompound serialized = this.selected.serializeNBT();

        this.label.setText(serialized.getString("Label"));

        if (serialized.hasKey("Color"))
        {
            this.primaryColorValue = serialized.getInteger("Color");
            this.primaryColor.picker.setColor(this.primaryColorValue);
        }
        else
        {
            this.primaryColorValue = 0xffffffff;
            this.primaryColor.picker.setColor(this.primaryColorValue);
        }

        if (serialized.hasKey("Background"))
        {
            this.backgroundColorValue = serialized.getInteger("Background");
            this.backgroundColor.picker.setColor(this.backgroundColorValue);
        }
        else
        {
            this.backgroundColorValue = 0xff6f5dd6;
            this.backgroundColor.picker.setColor(this.backgroundColorValue);
        }

        Graphic graphic = this.getPrimaryGraphic();

        if (graphic != null)
        {
            this.primaryColorValue = graphic.primary;
            this.primaryColor.picker.setColor(this.primaryColorValue);
        }

        if (graphic instanceof GradientGraphic)
        {
            this.secondaryColorValue = ((GradientGraphic) graphic).secondary;
            this.secondaryColor.picker.setColor(this.secondaryColorValue);
        }
        else if (graphic instanceof ShadowGraphic)
        {
            this.secondaryColorValue = ((ShadowGraphic) graphic).secondary;
            this.secondaryColor.picker.setColor(this.secondaryColorValue);
            this.shadowOffset.setValue(((ShadowGraphic) graphic).offset);
        }
        else
        {
            this.secondaryColorValue = 0x00000000;
            this.secondaryColor.picker.setColor(this.secondaryColorValue);
            this.shadowOffset.setValue(0);
        }

        if (graphic instanceof ImageGraphic && ((ImageGraphic) graphic).picture != null)
        {
            this.image.setText(((ImageGraphic) graphic).picture.toString());
        }
        else
        {
            this.image.setText("");
        }

        if (this.selected instanceof UIMorphComponent)
        {
            this.morph.setMorph(MorphManager.INSTANCE.morphFromNBT(((UIMorphComponent) this.selected).morph));
        }
        else
        {
            this.morph.setMorph(null);
        }

        this.syncing = false;
    }

    private void rebuildPreview()
    {
        this.previewHolder.removeAll();

        if (this.data == null)
        {
            return;
        }

        UIContext context = new UIContext(this.data);
        GuiElement preview = this.data.root.create(this.mc, context);

        preview.flex().relative(this.previewHolder).wh(1F, 1F);
        this.previewHolder.add(preview);
        this.previewHolder.resize();
    }

    @Override
    public boolean needsBackground()
    {
        return false;
    }

    @Override
    public IContentType<UI> getType()
    {
        return ContentTypes.UI;
    }

    @Override
    public String getTitle()
    {
        return "mappet.gui.panels.ui_constructor";
    }

    @Override
    public void fill(UI data, String editorName)
    {
        super.fill(data, editorName);

        this.editor.setVisible(data != null);

        if (data == null)
        {
            this.selected = null;
            this.rebuildList();
            this.syncProperties();
            this.rebuildPreview();
            return;
        }

        List<UIComponent> list = this.data.root.getChildComponents();

        this.selected = list.isEmpty() ? null : list.get(0);

        this.updateTooltips();
        this.rebuildList();
        this.syncProperties();
        this.rebuildPreview();
    }

    private GuiButtonElement createActionButton(String label, Runnable action)
    {
        GuiButtonElement button = new GuiButtonElement(this.mc, IKey.str(label), (b) -> action.run());

        button.flex().h(18);

        return button;
    }

    private GuiSimpleContextMenu createComponentsContextMenu()
    {
        if (this.selected == null) return null;

        GuiSimpleContextMenu menu = new GuiSimpleContextMenu(this.mc);
        menu.action(Icons.REMOVE, IKey.str("Удалить компонент"), this::removeSelected);

        return menu.shadow();
    }

    private void copyGeneratedCode()
    {
        GuiScreen.setClipboardString(this.generateScriptCode());
    }

    private String generateScriptCode()
    {
        StringBuilder code = new StringBuilder();
        Map<String, Integer> varCounters = new HashMap<>();
        List<String> buttonIds = new ArrayList<>();
        int morphIndex = 0;

        code.append("function main(c) {\n");
        code.append("    var ui = mappet.createUI(c, \"handler\").background();\n\n");

        if (this.data != null)
        {
            for (UIComponent component : this.data.root.getChildComponents())
            {
                String type = this.typeName(component).toLowerCase(Locale.ROOT);
                String var = this.nextVar(type, varCounters);
                String id = component.id == null ? "" : component.id;

                if (component instanceof UIButtonComponent)
                {
                    UIButtonComponent button = (UIButtonComponent) component;
                    String labelText = this.escapeJs(button.label);
                    code.append("    var ").append(var).append(" = ui.button(\"").append(labelText).append("\")");
                    if (!id.isEmpty()) code.append(".id(\"").append(this.escapeJs(id)).append("\")");
                    code.append(";\n");

                    NBTTagCompound tag = button.serializeNBT();
                    if (tag.hasKey("Background"))
                    {
                        code.append("    ").append(var).append(".background(").append(this.hex(tag.getInteger("Background"))).append(");\n");
                    }

                    if (!id.isEmpty()) buttonIds.add(id);
                }
                else if (component instanceof UIMorphComponent)
                {
                    UIMorphComponent morph = (UIMorphComponent) component;
                    String morphVar = "morph_" + (++morphIndex);
                    String morphNbt = morph.morph == null || morph.morph.hasNoTags() ? "{Name:\"blockbuster.steve\"}" : morph.morph.toString();

                    code.append("    var ").append(morphVar).append(" = mappet.createMorph('").append(this.escapeJs(morphNbt)).append("');\n");
                    code.append("    var ").append(var).append(" = ui.morph(").append(morphVar).append(", false)");
                    if (!id.isEmpty()) code.append(".id(\"").append(this.escapeJs(id)).append("\")");
                    code.append(";\n");
                    code.append("    ").append(var).append(".position(")
                        .append(this.fmt(morph.pos == null ? 0 : morph.pos.x)).append(", ")
                        .append(this.fmt(morph.pos == null ? 1 : morph.pos.y)).append(", ")
                        .append(this.fmt(morph.pos == null ? 0 : morph.pos.z)).append(")")
                        .append(".rotation(")
                        .append(this.fmt(morph.rot == null ? 0 : morph.rot.x)).append(", ")
                        .append(this.fmt(morph.rot == null ? 0 : morph.rot.y)).append(")")
                        .append(".distance(").append(this.fmt(morph.distance)).append(")")
                        .append(".fov(").append(this.fmt(morph.fov)).append(")")
                        .append(".editing(false);\n");
                }
                else if (component instanceof UIGraphicsComponent)
                {
                    UIGraphicsComponent graphics = (UIGraphicsComponent) component;
                    code.append("    var ").append(var).append(" = ui.graphics()");
                    if (!id.isEmpty()) code.append(".id(\"").append(this.escapeJs(id)).append("\")");
                    code.append(";\n");

                    Graphic graphic = graphics.graphics.isEmpty() ? null : graphics.graphics.get(0);

                    if (graphic instanceof GradientGraphic)
                    {
                        GradientGraphic g = (GradientGraphic) graphic;
                        code.append("    ").append(var).append(".gradient(")
                            .append(this.hex(g.primary)).append(", ")
                            .append(this.hex(g.secondary)).append(", ")
                            .append(g.horizontal).append(");\n");
                    }
                    else if (graphic instanceof ShadowGraphic)
                    {
                        ShadowGraphic g = (ShadowGraphic) graphic;
                        code.append("    ").append(var).append(".shadow(")
                            .append(this.hex(g.primary)).append(", ")
                            .append(this.hex(g.secondary)).append(", ")
                            .append(g.offset).append(");\n");
                    }
                    else if (graphic instanceof ImageGraphic)
                    {
                        ImageGraphic g = (ImageGraphic) graphic;
                        String rl = g.picture == null ? "minecraft:textures/gui/options_background.png" : g.picture.toString();
                        int tw = g.width <= 0 ? 16 : g.width;
                        int th = g.height <= 0 ? 16 : g.height;

                        code.append("    ").append(var).append(".image(\"")
                            .append(this.escapeJs(rl)).append("\", 0, 0, ")
                            .append(component.w.offset).append(", ")
                            .append(component.h.offset).append(", ")
                            .append(tw).append(", ")
                            .append(th).append(", ")
                            .append(this.hex(g.primary)).append(");\n");
                    }
                    else if (graphic != null)
                    {
                        code.append("    ").append(var).append(".rect(").append(this.hex(graphic.primary)).append(");\n");
                    }
                    else
                    {
                        code.append("    ").append(var).append(".rect(0xffffffff);\n");
                    }
                }
                else if (component instanceof UILabelBaseComponent)
                {
                    UILabelBaseComponent label = (UILabelBaseComponent) component;
                    String labelText = this.escapeJs(label.label);
                    code.append("    var ").append(var).append(" = ui.label(\"").append(labelText).append("\")");
                    if (!id.isEmpty()) code.append(".id(\"").append(this.escapeJs(id)).append("\")");
                    code.append(";\n");

                    NBTTagCompound tag = label.serializeNBT();
                    if (tag.hasKey("Color"))
                    {
                        code.append("    ").append(var).append(".color(").append(this.hex(tag.getInteger("Color"))).append(");\n");
                    }
                    if (tag.hasKey("Background"))
                    {
                        code.append("    ").append(var).append(".background(").append(this.hex(tag.getInteger("Background"))).append(");\n");
                    }
                }
                else
                {
                    code.append("    // Unsupported component: ").append(type).append("\n");
                    continue;
                }

                code.append("    ").append(var)
                    .append(".xy(").append(component.x.offset).append(", ").append(component.y.offset).append(")")
                    .append(".wh(").append(component.w.offset).append(", ").append(component.h.offset).append(")")
                    .append(".anchor(").append(this.fmt(component.x.anchor)).append(", ").append(this.fmt(component.y.anchor)).append(");\n\n");
            }
        }

        code.append("    c.getSubject().openUI(ui);\n");
        code.append("}\n\n");
        code.append("function handler(c) {\n");
        code.append("    var uiContext = c.getSubject().getUIContext();\n");
        code.append("    var last = uiContext.getLast();\n");

        if (buttonIds.isEmpty())
        {
            code.append("    // TODO: add handler logic\n");
        }
        else
        {
            code.append("    // Button handlers\n");
            for (String id : buttonIds)
            {
                String escaped = this.escapeJs(id);
                code.append("    if (last === \"").append(escaped).append("\") {\n");
                code.append("        // TODO: handle ").append(escaped).append("\n");
                code.append("    }\n");
            }
        }

        code.append("}\n");

        return code.toString();
    }

    private String nextVar(String base, Map<String, Integer> counters)
    {
        int next = counters.getOrDefault(base, 0) + 1;
        counters.put(base, next);

        return base + next;
    }

    private String hex(int color)
    {
        return String.format(Locale.ROOT, "0x%08x", color);
    }

    private String fmt(float value)
    {
        if (Math.abs(value - Math.round(value)) < 0.00001f)
        {
            return Integer.toString(Math.round(value));
        }

        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private String escapeJs(String value)
    {
        if (value == null) return "";

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "");
    }
}
