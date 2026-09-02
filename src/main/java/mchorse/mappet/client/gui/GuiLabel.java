package mchorse.mappet.client.gui;

import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.IconContainer;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;

// just fix render
public class GuiLabel extends mchorse.mclib.client.gui.framework.elements.utils.GuiLabel {

    public GuiLabel(Minecraft mc, IKey label, @Nullable Icon leftIcon, @Nullable Icon rightIcon) {
        super(mc, label, leftIcon, rightIcon);
    }

    public GuiLabel(Minecraft mc, IKey label, int color, @Nullable IconContainer leftIcon, @Nullable IconContainer rightIcon) {
        super(mc, label, color, leftIcon, rightIcon);
    }

    public GuiLabel(Minecraft mc, IKey label, int color, @Nullable Icon leftIcon, @Nullable Icon rightIcon) {
        super(mc, label, color, leftIcon, rightIcon);
    }

    public GuiLabel(Minecraft mc, IKey label, int color) {
        super(mc, label, color);
    }

    public GuiLabel(Minecraft mc, IKey label) {
        super(mc, label);
    }

    @Override
    public void draw(GuiContext context) {
        String text = label.get();

        int textWidth = font.getStringWidth(text);
        int textHeight = font.FONT_HEIGHT;

        IconContainer leftIcon = getLeftIcon();
        IconContainer rightIcon = getRightIcon();

        int leftWidth = leftIcon != null ? leftIcon.getW() + 4 : 0;
        int rightWidth = rightIcon != null ? rightIcon.getW() : 0;

        int totalWidth = leftWidth + textWidth + rightWidth;

        int x = area.x(anchorX, totalWidth);
        int y = area.y(anchorY, textHeight);

        int background = getColor();
        if (background >>> 24 != 0) {
            int padding = 5;
            Gui.drawRect(x, y - padding, x + totalWidth + padding * 2, y + textHeight + padding, background);
            x += padding;
        }

        int textX = x + leftWidth;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (leftIcon != null) {
            int iconY = y + (textHeight - leftIcon.getH()) / 2;
            leftIcon.render(x, iconY);
        }

        if (rightIcon != null) {
            int iconY = y + (textHeight - rightIcon.getH()) / 2;
            rightIcon.render(textX + textWidth, iconY);
        }

        font.drawString(text, textX, y, color, textShadow);
    }
}
