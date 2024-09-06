package mchorse.mappet.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class GuiExtendedIconElement extends GuiIconElement {

    public float rotate = 0;

    public GuiExtendedIconElement(Minecraft mc, Icon icon, Consumer<GuiIconElement> callback) {
        super(mc, icon, callback);
        this.rotate = 0;
    }

    public GuiExtendedIconElement(Minecraft mc, Icon icon, int color, Consumer<GuiIconElement> callback) {
        super(mc, icon, callback);
        this.iconColor = color;
    }

    public GuiExtendedIconElement(Minecraft mc, Icon icon, int color, float rotate, Consumer<GuiIconElement> callback) {
        super(mc, icon, callback);
        this.iconColor = color;
        this.rotate = rotate;
    }

    @Override
    public GuiExtendedIconElement both(Icon icon) {
        this.icon = this.hoverIcon = icon;
        return this;
    }

    @Override
    public GuiExtendedIconElement icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public GuiExtendedIconElement hovered(Icon icon) {
        this.hoverIcon = icon;
        return this;
    }

    @Override
    public GuiExtendedIconElement iconColor(int color) {
        this.iconColor = color;
        return this;
    }

    @Override
    public GuiExtendedIconElement hoverColor(int color) {
        this.hoverColor = color;
        return this;
    }

    @Override
    public GuiExtendedIconElement disabledColor(int color) {
        this.disabledColor = color;
        return this;
    }

    public GuiExtendedIconElement rotate(float angle) {
        this.rotate = angle;
        return this;
    }

    @Override
    protected GuiExtendedIconElement get() {
        return this;
    }

    @Override
    protected void drawSkin(GuiContext context) {
        Icon icon = this.hover ? this.hoverIcon : this.icon;
        int color = this.isEnabled() ? (this.hover ? this.hoverColor : this.iconColor) : disabledColor;
        ColorUtils.bindColor(color);
        if (rotate % 360 == 0) icon.render(this.area.mx(), this.area.my(), 0.5F, 0.5F);
        else render(icon, this.area.mx(), this.area.my(), 0.5F, 0.5F);
    }

    @SideOnly(Side.CLIENT)
    public void render(Icon icon, int x, int y, float ax, float ay) {
        if (icon.location != null) {
            x = (int) ((float) x - ax * (float) icon.w);
            y = (int) ((float) y - ay * (float) icon.h);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().renderEngine.bindTexture(icon.location);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + icon.w * ax, y + icon.h * ay, 0);
            GlStateManager.rotate(rotate % 360, 0, 0, 1);  // Adjust rotation as needed
            GlStateManager.translate(-icon.w * ax, -icon.h * ay, 0);

            GuiDraw.drawBillboard(0, 0, icon.x, icon.y, icon.w, icon.h, icon.textureW, icon.textureH);

            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
        }
    }
}

