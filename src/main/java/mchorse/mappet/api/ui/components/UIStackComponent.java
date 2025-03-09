package mchorse.mappet.api.ui.components;

import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.ui.utils.DiscardMethod;
import mchorse.mappet.client.gui.ExtendedGuiSlotElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UIStackComponent extends UIComponent {
    public ItemStack stack = ItemStack.EMPTY;
    public boolean locked = false;
    public boolean frameVisible = true;
    public boolean itemTooltipVisible = true;

    public UIStackComponent stack(IScriptItemStack stack) {
        return this.stack(stack == null ? ItemStack.EMPTY : stack.getMinecraftItemStack());
    }

    public UIStackComponent stack(ItemStack stack) {
        this.change("Stack");
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        return this;
    }

    public IScriptItemStack getStack() {
        return ScriptItemStack.create(stack);
    }

    public UIStackComponent lock() {
        return setLocked(true);
    }

    public UIStackComponent setLocked(boolean locked) {
        this.change("Lock");
        this.locked = locked;
        return this;
    }

    public UIStackComponent withoutFrame() {
        return frameVisible(false);
    }

    public UIStackComponent frameVisible(boolean visible) {
        this.change("FrameVisible");
        this.frameVisible = visible;
        return this;
    }

    public UIStackComponent hideItemTooltip() {
        return itemTooltipVisible(false);
    }

    public UIStackComponent itemTooltipVisible(boolean visible) {
        this.change("TooltipVisible");
        this.itemTooltipVisible = visible;
        return this;
    }

    @Override
    @DiscardMethod
    protected int getDefaultUpdateDelay() {
        return UIComponent.DELAY;
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    protected void applyProperty(UIContext context, String key, GuiElement element) {
        super.applyProperty(context, key, element);

        if (key.equals("Stack") && element instanceof GuiSlotElement) {
            ((GuiSlotElement) element).setStack(stack);
        } else if (element instanceof ExtendedGuiSlotElement) {
            switch (key) {
                case "Lock":
                    ((ExtendedGuiSlotElement) element).locked = locked;
                    break;
                case "FrameVisible":
                    ((ExtendedGuiSlotElement) element).frameVisible = frameVisible;
                    break;
                case "TooltipVisible":
                    ((ExtendedGuiSlotElement) element).itemTooltipVisible = itemTooltipVisible;
                    break;
            }
        }
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    public GuiElement create(Minecraft mc, UIContext context) {
        final ExtendedGuiSlotElement element = new ExtendedGuiSlotElement(mc, 0, null);

        element.callback = id.isEmpty() ? null : (stack) -> {
            context.data.setTag(id, stack.serializeNBT());
            context.data.setInteger(id + ".slot", element.lastSlot);
            context.dirty(id, updateDelay);
        };
        element.setStack(stack);
        element.locked = locked;
        element.frameVisible = frameVisible;
        element.itemTooltipVisible = itemTooltipVisible;
        element.drawDisabled = false;

        return this.apply(element, context);
    }

    @Override
    @DiscardMethod
    public void populateData(NBTTagCompound tag) {
        super.populateData(tag);
        if (id.isEmpty()) return;
        tag.setTag(id, stack.serializeNBT());
        tag.setBoolean(id + ".locked", locked);
    }

    @Override
    @DiscardMethod
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);
        tag.setTag("Stack", stack.serializeNBT());
        tag.setBoolean("Lock", locked);
        tag.setBoolean("FrameVisible", frameVisible);
        tag.setBoolean("TooltipVisible", itemTooltipVisible);
    }

    @Override
    @DiscardMethod
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        if (tag.hasKey("Stack")) stack = new ItemStack(tag.getCompoundTag("Stack"));
        if (tag.hasKey("Lock")) locked = tag.getBoolean("Lock");
        if (tag.hasKey("FrameVisible")) frameVisible = tag.getBoolean("FrameVisible");
        if (tag.hasKey("TooltipVisible")) itemTooltipVisible = tag.getBoolean("TooltipVisible");
    }
}
