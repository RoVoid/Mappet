package mchorse.mappet.api.ui.components;

import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.mappet.IMappetUIBuilder;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.ui.utils.DiscardMethod;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Item stack (slot) UI component.
 *
 * <p>This component allows users to input an item stack. The value that gets written
 * to UI context's data (if ID is present) is an NBT compound tag.</p>
 *
 * <p>This component can be created using {@link IMappetUIBuilder#item()} method.</p>
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        var ui = mappet.createUI(c, "handler").background();
 *        var stack = ui.item().id("stack").tooltip("To dupe an item, please pick that item.");
 *
 *        stack.rxy(0.5, 0.5).wh(24, 24).anchor(0.5);
 *        c.getSubject().openUI(ui);
 *    }
 *
 *    function handler(c)
 *    {
 *        var uiContext = c.getSubject().getUIContext();
 *        var data = uiContext.getData();
 *
 *        if (uiContext.getLast() === "stack")
 *        {
 *            var item = mappet.createItem(data.getCompound("stack"));
 *            var pos = c.getSubject().getPosition();
 *
 *            c.getWorld().dropItemStack(item, pos.x, pos.y + 2, pos.z);
 *
 *            // Item stack UI component also includes the slot from which
 *            // item was picked from player's inventory. -1 means it was
 *            // picked from elsewhere (from search or pasted into the
 *            // field)
 *            var slot = data.getInt("stack.slot");
 *
 *            if (slot >= 0)
 *            {
 *                // When slot isn't -1, you can access it from player's inventory
 *                var corresponding = c.getSubject().getInventory().getStack(slot);
 *
 *                c.send("Item " + corresponding.getMinecraftItemStack() + " was taken from slot " + slot);
 *            }
 *        }
 *    }
 * }</pre>
 */
public class UIStackComponent extends UIComponent
{
    public ItemStack stack = ItemStack.EMPTY;
    public boolean locked = false;

    /**
     * Set item stack component's item from scripts.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var ui = mappet.createUI().background();
     *        var stack = ui.item().id("stack").tooltip("An exhibit D.", 1);
     *
     *        stack.rxy(0.5, 0.5).wh(24, 24).anchor(0.5);
     *        stack.stack(mappet.createItem("minecraft:diamond_sword"));
     *        c.getSubject().openUI(ui);
     *    }
     * }</pre>
     */
    public UIStackComponent stack(IScriptItemStack stack)
    {
        return this.stack(stack == null ? null : stack.getMinecraftItemStack());
    }

    /**
     * Set item stack component's item. See the example in {@link #stack(IScriptItemStack)}.
     */
    public UIStackComponent stack(ItemStack stack)
    {
        this.change("Stack");

        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();

        return this;
    }

    public UIStackComponent lock()
    {
        return setLocked(true);
    }

    public UIStackComponent setLocked(boolean locked)
    {
        this.change("Lock");

        this.locked = locked;

        return this;
    }

    @Override
    @DiscardMethod
    protected int getDefaultUpdateDelay()
    {
        return UIComponent.DELAY;
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    protected void applyProperty(UIContext context, String key, GuiElement element)
    {
        super.applyProperty(context, key, element);

        if (key.equals("Stack"))
        {
            ((GuiSlotElement) element).setStack(this.stack);
        }
        else if (key.equals("Lock"))
        {
            ((AbstractGuiSlotElement) element).locked = this.locked;
        }
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    public GuiElement create(Minecraft mc, UIContext context)
    {
        final GuiSlotElement element = new AbstractGuiSlotElement(mc, 0, null);


        element.callback = this.id.isEmpty() ? null : (stack) ->
        {
            context.data.setTag(this.id, stack.serializeNBT());
            context.data.setInteger(this.id + ".slot", element.lastSlot);
            context.dirty(this.id, this.updateDelay);
        };
        element.setStack(this.stack);
        element.drawDisabled = false;

        return this.apply(element, context);
    }

    @Override
    @DiscardMethod
    public void populateData(NBTTagCompound tag)
    {
        super.populateData(tag);

        if (!this.id.isEmpty())
        {
            tag.setTag(this.id, this.stack.serializeNBT());
        }
    }

    @Override
    @DiscardMethod
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);

        tag.setTag("Stack", this.stack.serializeNBT());
        tag.setBoolean("Lock", locked);
    }

    @Override
    @DiscardMethod
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);

        if (tag.hasKey("Stack"))
        {
            this.stack = new ItemStack(tag.getCompoundTag("Stack"));
        }
        else if (tag.hasKey("Lock"))
        {
            locked = tag.getBoolean("Lock");
        }
    }

    private static class AbstractGuiSlotElement extends GuiSlotElement{

        public boolean locked = false;

        public AbstractGuiSlotElement(Minecraft mc, int slot, Consumer<ItemStack> callback) {
            super(mc, slot, callback);
        }

        @Override
        protected void click(int mouseButton) {
            if(!locked) super.click(mouseButton);
        }
    }
}