package mchorse.mappet.api.ui.components;

import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.ui.utils.DiscardMethod;
import mchorse.mappet.client.gui.AbstractGuiSlotElement;
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

    public UIStackComponent stack(IScriptItemStack stack) {
        return this.stack(stack == null ? ItemStack.EMPTY : stack.getMinecraftItemStack());
    }

    public UIStackComponent stack(ItemStack stack) {
        this.change("Stack");
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        return this;
    }

    public UIStackComponent lock() {
        return setLocked(true);
    }

    public UIStackComponent setLocked(boolean locked) {
        this.change("Lock");
        this.locked = locked;
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
            ((GuiSlotElement) element).setStack(this.stack);
        } else if (key.equals("Lock") && element instanceof AbstractGuiSlotElement) {
            ((AbstractGuiSlotElement) element).locked = this.locked;
        }
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    public GuiElement create(Minecraft mc, UIContext context) {
        final AbstractGuiSlotElement element = new AbstractGuiSlotElement(mc, 0, null);

        element.callback = id.isEmpty() ? null : (stack) -> {
            context.data.setTag(id, stack.serializeNBT());
            context.data.setBoolean(id + ".locked", locked);
            context.data.setInteger(id + ".slot", element.lastSlot);
            context.dirty(id, updateDelay);
        };
        element.setStack(stack);
        element.locked = locked;
        element.drawDisabled = false;

        return this.apply(element, context);
    }

    @Override
    @DiscardMethod
    public void populateData(NBTTagCompound tag) {
        super.populateData(tag);
        if (!id.isEmpty()) {
            tag.setTag(id, stack.serializeNBT());
            tag.setBoolean(id + ".locked", locked);
        }
    }

    @Override
    @DiscardMethod
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);
        tag.setTag("Stack", stack.serializeNBT());
        tag.setBoolean("Lock", locked);
    }

    @Override
    @DiscardMethod
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        if (tag.hasKey("Stack")) {
            stack = new ItemStack(tag.getCompoundTag("Stack"));
        }
        if (tag.hasKey("Lock")) {
            locked = tag.getBoolean("Lock");
        }
    }
}
