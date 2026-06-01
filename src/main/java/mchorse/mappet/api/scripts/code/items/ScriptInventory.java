package mchorse.mappet.api.scripts.code.items;

import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockableLoot;

public class ScriptInventory {
    private final IInventory inventory;

    public ScriptInventory(IInventory inventory) {
        this.inventory = inventory;
    }

    @Deprecated
    public IInventory getMinecraftInventory() {
        return inventory;
    }

    public IInventory asMinecraft() {
        return inventory;
    }

    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    public int size() {
        return this.inventory.getSizeInventory();
    }

    public ScriptItemStack getStack(int index) {
        return ScriptItemStack.create(this.inventory.getStackInSlot(index));
    }

    public ScriptItemStack removeStack(int index) {
        return ScriptItemStack.create(this.inventory.removeStackFromSlot(index));
    }

    public void setStack(int index, ScriptItemStack stack) {
        if (stack == null) {
            stack = ScriptItemStack.EMPTY;
        }

        if (index >= 0 && index < this.size()) {
            this.inventory.setInventorySlotContents(index, stack.getMinecraftItemStack());
        }
    }

    public void clear() {
        for (int i = 0, c = this.inventory.getSizeInventory(); i < c; i++) {
            this.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    /* Basic inventory */

    public String getName() {
        return this.inventory.getName();
    }

    public boolean hasCustomName() {
        return this.inventory.hasCustomName();
    }

    public void setName(String name) {
        if (this.inventory instanceof InventoryBasic) {
            ((InventoryBasic) this.inventory).setCustomName(name);
        } else if (this.inventory instanceof TileEntityLockableLoot) {
            ((TileEntityLockableLoot) this.inventory).setCustomName(name);
        }
    }
}