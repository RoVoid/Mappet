package mchorse.mappet.client.gui.utils;

import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlayPanel;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiConfirmOverlayPanel extends GuiOverlayPanel {
    public Consumer<Boolean> callback;
    public GuiButtonElement confirm;
    public GuiButtonElement cancel;

    public GuiConfirmOverlayPanel(Minecraft mc, IKey label, Consumer<Boolean> callback) {
        super(mc, label);
        this.callback = callback;
        background = 1;

        confirm = new GuiButtonElement(mc, IKey.lang("mclib.gui.ok"), (b) -> send(true));
        cancel = new GuiButtonElement(mc, IKey.lang("mclib.gui.cancel"), (b) -> send(false));
        GuiElement row = Elements.row(mc, 5, confirm, cancel);
        row.flex().relative(content).w(0.9F).x(0.5F).y(0.5F, -10).anchor(0.5F, 0.5F);

        content.add(row);

        GuiBase.getCurrent().unfocus();
    }

    public void send(boolean result) {
        close();
        if (callback != null) callback.accept(result);
    }

    public void open() {
        GuiOverlay.addOverlay(GuiBase.getCurrent(), this, 200, 70);
    }
}
