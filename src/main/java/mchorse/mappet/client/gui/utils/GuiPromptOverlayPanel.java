package mchorse.mappet.client.gui.utils;

import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlayPanel;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;
import java.util.function.Function;

public class GuiPromptOverlayPanel extends GuiOverlayPanel {
    private final Consumer<String> callback;
    private final Function<String, IKey> validator;

    public GuiTextElement text;
    public GuiButtonElement confirm;
    public GuiButtonElement cancel;

    public GuiPromptOverlayPanel(Minecraft mc, IKey label, Consumer<String> callback) {
        this(mc, label, callback, null);
    }

    public GuiPromptOverlayPanel(Minecraft mc, IKey label, Consumer<String> callback, Function<String, IKey> validator) {
        super(mc, label);
        background = 1;

        this.callback = callback;
        this.validator = validator;

        GuiElement column = Elements.column(mc, 5);
        column.flex().relative(content).w(0.9F).x(0.5F).y(0.5F, -10).anchor(0.5F, 0.5F);

        text = new GuiTextElement(mc, validator == null ? null : this::execValidator);
        confirm = new GuiButtonElement(mc, IKey.lang("mclib.gui.ok"), (b) -> send());
        cancel = new GuiButtonElement(mc, IKey.lang("mclib.gui.cancel"), (b) -> close());
        GuiElement row = Elements.row(mc, 5, confirm, cancel);

        column.add(text, row);
        content.add(column);

        setValue("");
        GuiBase.getCurrent().focus(text);
    }

    public GuiPromptOverlayPanel filename() {
        text.filename();
        return this;
    }

    public GuiPromptOverlayPanel setValue(String value) {
        if (value == null) value = "";

        text.field.setText(value);
        text.field.setCursorPositionEnd();
        text.field.lineScrollOffset = 0;

        execValidator(text.field.getText());
        return this;
    }

    public void send() {
        String text = this.text.field.getText();
        if (text.isEmpty()) return;
        close();
        if (callback != null) callback.accept(text);
    }

    public boolean keyTyped(GuiContext context) {
        if (super.keyTyped(context)) return true;
        if (context.keyCode == Keyboard.KEY_RETURN) {
            confirm.clickItself(context);
            return true;
        }
        if (context.keyCode == Keyboard.KEY_ESCAPE) {
            cancel.clickItself(context);
            return true;
        }
        return false;
    }

    public void execValidator(String text) {
        if (validator == null) return;

        IKey error = validator.apply(text);
        boolean valid = error == null || error.get().isEmpty();

        this.text.setTextColor(valid ? 14737632 : 0xff0033);
        confirm.setEnabled(valid);

        if (valid) confirm.removeTooltip();
        else confirm.tooltip(error);
    }

    public void open() {
        GuiOverlay.addOverlay(GuiBase.getCurrent(), this, 200, 100);
    }
}
