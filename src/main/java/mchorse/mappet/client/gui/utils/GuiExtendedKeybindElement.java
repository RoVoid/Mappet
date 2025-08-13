package mchorse.mappet.client.gui.utils;

import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.input.GuiKeybindElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.utils.Keys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.function.Consumer;

public class GuiExtendedKeybindElement extends GuiKeybindElement {
    public int background = 0;
    public int waitBackground = McLib.primaryColor.get();
    public int color = 16777215;

    public GuiExtendedKeybindElement(Minecraft mc, Consumer<Integer> callback) {
        super(mc, callback);
    }

    @Override
    public void draw(GuiContext context) {
        if (enabled) {
            GuiDraw.drawBorder(area, -16777216 + waitBackground);
            int x = area.mx();
            int y = area.my();
            int a = (int) (Math.sin((double) ((float) context.tick + context.partialTicks) / (double) 2.0F) * (double) 127.5F + (double) 127.5F) << 24;
            Gui.drawRect(x - 1, y - 6, x + 1, y + 6, a + 16777215);
            if (comboKey) {
                checkHolding();
            }
        }
        else {
            area.draw(-16777216 + background);
            drawCenteredString(font, Keys.getComboKeyName(keybind), area.mx(), area.my() - font.FONT_HEIGHT / 2, color);
        }

        if (tooltip != null && area.isInside(context)) {
            context.tooltip.set(context, this);
        }
        else if ((hideTooltip || container) && area.isInside(context)) {
            context.resetTooltip();
        }
    }
}
