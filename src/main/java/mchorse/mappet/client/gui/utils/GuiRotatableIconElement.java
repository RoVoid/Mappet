package mchorse.mappet.client.gui.utils;

import mchorse.mappet.Mappet;
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

public class GuiRotatableIconElement extends GuiIconElement {

    public float rotate = 0;

    public GuiRotatableIconElement(Minecraft mc, Icon icon, Consumer<GuiIconElement> callback) {
        super(mc, icon, callback);
    }

    public GuiRotatableIconElement rotate(float angle) { // degrees
        rotate = angle;
        return this;
    }

    @Override
    public GuiRotatableIconElement both(Icon icon) {
        this.icon = hoverIcon = icon;
        return this;
    }

    @Override
    public GuiRotatableIconElement icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public GuiRotatableIconElement hovered(Icon icon) {
        hoverIcon = icon;
        return this;
    }

    @Override
    public GuiRotatableIconElement iconColor(int color) {
        iconColor = color;
        return this;
    }

    @Override
    public GuiRotatableIconElement hoverColor(int color) {
        hoverColor = color;
        return this;
    }

    @Override
    public GuiRotatableIconElement disabledColor(int color) {
        disabledColor = color;
        return this;
    }

    @Override
    protected GuiRotatableIconElement get() {
        return this;
    }

    @Override
    protected void drawSkin(GuiContext context) {
        Icon icon = hover ? hoverIcon : this.icon;
        int color = isEnabled() ? hover ? hoverColor : iconColor : disabledColor;
        ColorUtils.bindColor(color);
        if (rotate % 360 == 0) icon.render(area.mx(), area.my(), 0.5f, 0.5f);
        else if (icon instanceof AnimatedIcon) ((AnimatedIcon) icon).render(area.mx(), area.my(), 0.5f, 0.5f, rotate);
        else render(icon, area.mx(), area.my(), 0.5f, 0.5f);
    }

    @SideOnly(Side.CLIENT)
    public void render(Icon icon, int x, int y, float ax, float ay) {
        Mappet.logger.debug(icon.toString());
        if (icon.location == null) return;

        x = (int) ((float) x - ax * (float) icon.w);
        y = (int) ((float) y - ay * (float) icon.h);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().renderEngine.bindTexture(icon.location);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + icon.w * ax, y + icon.h * ay, 0);
        GlStateManager.rotate(rotate % 360, 0, 0, 1);
        GlStateManager.translate(-icon.w * ax, -icon.h * ay, 0);

        GuiDraw.drawBillboard(0, 0, icon.x, icon.y, icon.w, icon.h, icon.textureW, icon.textureH);

        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }
}

