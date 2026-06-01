package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.proxy.CommonProxy;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.utils.LayoutType;
import mchorse.metamorph.api.morphs.AbstractMorph;

import java.util.List;

public class MappetUIBuilder {
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

    // TODO: Перенести в UI + Map
    public UIComponent get(String id) {
        return getByIdRecursive(id, current);
    }

    private UIComponent getByIdRecursive(String id, UIComponent component) {
        for (UIComponent child : component.getChildComponents()) {
            if (child.id.equals(id)) return child;

            UIComponent result = getByIdRecursive(id, child);
            if (result != null) return result;
        }

        return null;
    }

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

    public MappetUIBuilder background() {
        if (ui != null) ui.background = true;
        return this;
    }

    public MappetUIBuilder closable(boolean closable) {
        if (ui != null) ui.closable = closable;
        return this;
    }

    public MappetUIBuilder paused(boolean paused) {
        if (ui != null) ui.paused = paused;
        return this;
    }

    public MappetUIBuilder mouse(int flags){
        return mouse(flags, 5);
    }

    public MappetUIBuilder mouse(int flags, int delay){
        if (ui != null) {
            ui.mouseFlags = flags;
            ui.mouseDelay = delay;
        }
        return this;
    }

    public UIComponent create(String id) {
        UIComponent component = CommonProxy.getUiComponents().create(id);
        if (component == null) return null;

        current.getChildComponents().add(component);

        return component;
    }

    public UIGraphicsComponent graphics() {
        UIGraphicsComponent component = new UIGraphicsComponent();

        current.getChildComponents().add(component);

        return component;
    }

    public UIButtonComponent button(String label) {
        UIButtonComponent component = new UIButtonComponent();

        current.getChildComponents().add(component);
        component.label(label);

        return component;
    }

    public UIIconButtonComponent icon(String icon) {
        UIIconButtonComponent component = new UIIconButtonComponent();

        current.getChildComponents().add(component);
        component.icon(icon);

        return component;
    }

    public UIKeybindComponent keybind(int keycode) {
        UIKeybindComponent component = new UIKeybindComponent();

        current.getChildComponents().add(component);
        component.keycode = keycode;

        return component;
    }

    public UILabelComponent label(String label) {
        UILabelComponent component = new UILabelComponent();

        current.getChildComponents().add(component);
        component.label(label);

        return component;
    }

    public UITextComponent text(String text) {
        UITextComponent component = new UITextComponent();

        current.getChildComponents().add(component);
        component.label(text);

        return component;
    }

    public UITextboxComponent textbox(String text, int maxLength) {
        UITextboxComponent component = new UITextboxComponent();

        current.getChildComponents().add(component);
        component.maxLength(maxLength).label(text);

        return component;
    }

    public UITextareaComponent textarea(String text) {
        UITextareaComponent component = new UITextareaComponent();

        current.getChildComponents().add(component);
        component.label(text);

        return component;
    }

    public UIToggleComponent toggle(String label, boolean state) {
        UIToggleComponent component = new UIToggleComponent();

        current.getChildComponents().add(component);
        component.state(state).label(label);

        return component;
    }

    public UITrackpadComponent trackpad(double value) {
        UITrackpadComponent component = new UITrackpadComponent();

        current.getChildComponents().add(component);
        component.value(value);

        return component;
    }

    public UIStringListComponent stringList(List<String> values, int selected) {
        UIStringListComponent component = new UIStringListComponent();

        current.getChildComponents().add(component);
        component.values(values);

        if (selected >= 0) component.selected(selected);

        return component;
    }

    public UIDropdownComponent dropdown(List<String> values, int selected) {
        UIDropdownComponent component = new UIDropdownComponent();

        current.getChildComponents().add(component);
        component.values(values);

        if (selected >= 0) component.selected(selected);

        return component;
    }

    public UIStackComponent item(ScriptItemStack stack) {
        UIStackComponent component = new UIStackComponent();

        current.getChildComponents().add(component);

        if (stack != null && !stack.isEmpty()) {
            component.stack(stack.asMinecraft());
        }

        return component;
    }

    public UIMorphComponent morph(AbstractMorph morph, boolean editing) {
        UIMorphComponent component = new UIMorphComponent();

        current.getChildComponents().add(component);
        component.morph(morph);

        if (editing) component.editing();

        return component;
    }

    public UIClickComponent click() {
        UIClickComponent component = new UIClickComponent();

        current.getChildComponents().add(component);

        return component;
    }

    public MappetUIBuilder layout() {
        return new MappetUIBuilder(layout(0, 0));
    }

    public UILayoutComponent layout(int margin, int padding) {
        UILayoutComponent layout = new UILayoutComponent();

        layout.margin = margin;
        layout.padding = padding;
        current.getChildComponents().add(layout);

        return layout;
    }

    public MappetUIBuilder column(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.COLUMN;

        return new MappetUIBuilder(layout);
    }

    public MappetUIBuilder row(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.ROW;

        return new MappetUIBuilder(layout);
    }

    public MappetUIBuilder grid(int margin, int padding) {
        UILayoutComponent layout = layout(margin, padding);

        layout.layoutType = LayoutType.GRID;

        return new MappetUIBuilder(layout);
    }
}