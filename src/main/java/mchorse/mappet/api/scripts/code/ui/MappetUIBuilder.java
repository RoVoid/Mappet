package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.CommonProxy;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.ui.IMappetUIBuilder;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.utils.LayoutType;
import mchorse.metamorph.api.morphs.AbstractMorph;

import java.util.List;

public class MappetUIBuilder implements IMappetUIBuilder {
    private UI ui;
    private final UIComponent current;
    private String script;
    private String function;

    public MappetUIBuilder(UI ui, String script, String function) {
        this.ui = ui;
        current = ui.root;
        this.script = script;
        this.function = function;
    }

    public MappetUIBuilder(UIComponent component) {
        current = component;
    }

    @Override
    public UIComponent getCurrent() {
        return current;
    }

    public UI getUI() {
        return ui;
    }

    public String getScript() {
        return script;
    }

    public String getFunction() {
        return function;
    }

    @Override
    public IMappetUIBuilder background() {
        if (ui != null) ui.background = true;
        return this;
    }

    @Override
    public IMappetUIBuilder closable(boolean closable) {
        if (ui != null) ui.closable = closable;
        return this;
    }

    @Override
    public IMappetUIBuilder paused(boolean paused) {
        if (ui != null) ui.paused = paused;
        return this;
    }

    public IMappetUIBuilder mouse(int delay){
        if (ui != null) ui.mouse = delay;
        return this;
    }

    @Override
    public UIComponent create(String id) {
        UIComponent component = CommonProxy.getUiComponents().create(id);
        if (component == null) return null;

        current.getChildComponents().add(component);

        return component;
    }

    @Override
    public UIGraphicsComponent graphics() {
        UIGraphicsComponent component = new UIGraphicsComponent();

        current.getChildComponents().add(component);

        return component;
    }

    @Override
    public UIButtonComponent button(String label) {
        UIButtonComponent component = new UIButtonComponent();

        current.getChildComponents().add(component);
        component.label(label);

        return component;
    }

    @Override
    public UIIconButtonComponent icon(String icon) {
        UIIconButtonComponent component = new UIIconButtonComponent();

        current.getChildComponents().add(component);
        component.icon(icon);

        return component;
    }

    @Override
    public UIKeybindComponent keybind(int keycode) {
        UIKeybindComponent component = new UIKeybindComponent();

        current.getChildComponents().add(component);
        component.keycode = keycode;

        return component;
    }

    @Override
    public UILabelComponent label(String label) {
        UILabelComponent component = new UILabelComponent();

        current.getChildComponents().add(component);
        component.label(label);

        return component;
    }

    @Override
    public UITextComponent text(String text) {
        UITextComponent component = new UITextComponent();

        current.getChildComponents().add(component);
        component.label(text);

        return component;
    }

    @Override
    public UITextboxComponent textbox(String text, int maxLength) {
        UITextboxComponent component = new UITextboxComponent();

        current.getChildComponents().add(component);
        component.maxLength(maxLength).label(text);

        return component;
    }

    @Override
    public UITextareaComponent textarea(String text) {
        UITextareaComponent component = new UITextareaComponent();

        current.getChildComponents().add(component);
        component.label(text);

        return component;
    }

    @Override
    public UIToggleComponent toggle(String label, boolean state) {
        UIToggleComponent component = new UIToggleComponent();

        current.getChildComponents().add(component);
        component.state(state).label(label);

        return component;
    }

    @Override
    public UITrackpadComponent trackpad(double value) {
        UITrackpadComponent component = new UITrackpadComponent();

        current.getChildComponents().add(component);
        component.value(value);

        return component;
    }

    @Override
    public UIStringListComponent stringList(List<String> values, int selected) {
        UIStringListComponent component = new UIStringListComponent();

        current.getChildComponents().add(component);
        component.values(values);

        if (selected >= 0) component.selected(selected);

        return component;
    }

    @Override
    public UIStackComponent item(IScriptItemStack stack) {
        UIStackComponent component = new UIStackComponent();

        current.getChildComponents().add(component);

        if (stack != null && !stack.isEmpty()) {
            component.stack(stack.asMinecraft());
        }

        return component;
    }

    @Override
    public UIMorphComponent morph(AbstractMorph morph, boolean editing) {
        UIMorphComponent component = new UIMorphComponent();

        current.getChildComponents().add(component);
        component.morph(morph);

        if (editing) component.editing();

        return component;
    }

    @Override
    public UIClickComponent click() {
        UIClickComponent component = new UIClickComponent();

        current.getChildComponents().add(component);

        return component;
    }

    @Override
    public IMappetUIBuilder layout() {
        return new MappetUIBuilder(layout(0, 0));
    }

    public UILayoutComponent layout(int margin, int padding) {
        UILayoutComponent layout = new UILayoutComponent();

        layout.margin = margin;
        layout.padding = padding;
        current.getChildComponents().add(layout);

        return layout;
    }

    @Override
    public IMappetUIBuilder column(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.COLUMN;

        return new MappetUIBuilder(layout);
    }

    @Override
    public IMappetUIBuilder row(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.ROW;

        return new MappetUIBuilder(layout);
    }

    @Override
    public IMappetUIBuilder grid(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.GRID;

        return new MappetUIBuilder(layout);
    }
}