package mchorse.mappet.client.gui.states;

import mchorse.mappet.api.states.States;
import mchorse.mappet.utils.Colors;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiState extends GuiElement {
    public GuiTextElement id;
    public GuiIconElement convert;
    public GuiElement value;
    public GuiIconElement remove;

    private String key;
    private final States states;

    public GuiState(Minecraft mc, String key, States states) {
        super(mc);

        this.key = key;
        this.states = states;

        id = new GuiTextElement(mc, 1000, this::rename);
        id.flex().w(120);
        id.setText(key);
        convert = new GuiIconElement(mc, Icons.REFRESH, this::convert);
        remove = new GuiIconElement(mc, Icons.REMOVE, this::removeState);

        flex().row(0).preferred(2);
        updateValue();
    }

    public String getKey() {
        return key;
    }

    private void rename(String key) {
        if (states.has(key) || key.isEmpty()) {
            id.field.setTextColor(Colors.NEGATIVE);
            return;
        }

        id.field.setTextColor(0xffffff);

        Object value = states.values().remove(this.key);

        states.values().put(key, value);
        this.key = key;
    }

    private void convert(GuiIconElement element) {
        Object object = states.values().get(key);

        if (object instanceof Number) states.values().put(key, "");
        else if (object instanceof String) states.values().put(key, false);
        else states.values().put(key, 0);

        updateValue();
    }

    private void removeState(GuiIconElement element) {
        states.values().remove(key);

        GuiElement parent = getParentContainer();

        removeFromParent();
        parent.resize();
    }

    private void updateValue() {
        Object object = states.values().get(key);

        if (object instanceof Number) {
            GuiTrackpadElement element = new GuiTrackpadElement(mc, this::updateNumber);
            element.setValue(((Number) object).doubleValue());
            value = element;

        }
        else if (object instanceof String) {
            GuiTextElement element = new GuiTextElement(mc, 10000, this::updateString);
            element.setText((String) object);
            value = element;
        }
        else {
            boolean value = object instanceof Boolean && (Boolean) object;
            GuiButtonElement element = new GuiButtonElement(mc, IKey.str(value ? "true" : "false"), this::updateBoolean);
            element.setColor(value ? Colors.ACTIVE : Colors.NEGATIVE, false);
            element.color(-16777216);
            this.value = element;
        }

        removeAll();
        add(id, convert, value, remove);

        if (hasParent()) {
            getParentContainer().resize();
        }
    }

    private void updateNumber(double v) {
        states.values().put(key, v);
    }

    private void updateString(String s) {
        states.values().put(key, s);
    }

    private void updateBoolean(GuiButtonElement button) {
        Object object = states.values().get(key);
        boolean value = !(object instanceof Boolean && (Boolean) object);
        states.values().put(key, value);
        button.label = IKey.str(value ? "true" : "false");
        button.setColor(value ? Colors.ACTIVE : Colors.NEGATIVE, false);
    }
}