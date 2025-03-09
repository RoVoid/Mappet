package mchorse.mappet.client.gui;

import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiInventoryElement;
import mchorse.mclib.client.gui.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ExtendedGuiSlotElement extends GuiSlotElement {
    public boolean locked = false;
    public boolean frameVisible = true;
    public boolean itemTooltipVisible = true;

    public ExtendedGuiSlotElement(Minecraft mc, int slot, Consumer<ItemStack> callback) {
        super(mc, slot, callback);
    }

    @Override
    protected void click(int mouseButton) {
        if (!locked) super.click(mouseButton);
        else acceptStack(getStack(), -1);
    }

    @Override
    protected void drawSkin(GuiContext context) {
        if (frameVisible) super.drawSkin(context);
        else {
            int x = this.area.mx() - 8;
            int y = this.area.my() - 8;
            if (this.getStack().isEmpty() && this.slot != 0) {
                GlStateManager.enableAlpha();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                if (this.slot == 1) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(SHIELD);
                } else if (this.slot == 2) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(BOOTS);
                } else if (this.slot == 3) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(LEGGINGS);
                } else if (this.slot == 4) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(CHESTPLATE);
                } else if (this.slot == 5) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(HELMET);
                }

                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
            } else {
                RenderHelper.enableGUIStandardItemLighting();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GlStateManager.enableDepth();
                GuiInventoryElement.drawItemStack(this.getStack(), x, y, null);
                if (this.area.isInside(context)) {
                    context.tooltip.set(context, this);
                }

                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();
            }

            if (this.drawDisabled) {
                GuiDraw.drawLockedArea(this, McLib.enableBorders.get() ? 1 : 0);
            }
        }
    }

    @Override
    public void drawTooltip(GuiContext context, Area area) {
        if (itemTooltipVisible) super.drawTooltip(context, area);
        else context.tooltip.draw(this.tooltip, context);
    }
}
