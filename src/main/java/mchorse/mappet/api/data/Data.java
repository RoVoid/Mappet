package mchorse.mappet.api.data;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class Data extends AbstractData {
    public StatesProvider global = new StatesProvider();
    public StatesProvider player = new StatesProvider();
    public NonNullList<ItemStack> inventory = NonNullList.create();

    public void save(EntityPlayer player) {
        ICharacter character = Character.get(player);

        if (character == null) return;

        global.from(Mappet.states);
        this.player.from(character.getStates());

        for (int i = 0, c = player.inventory.getSizeInventory(); i < c; i++)
            inventory.add(player.inventory.getStackInSlot(i).copy());
    }

    public void apply(EntityPlayer player, boolean global) {
        ICharacter character = Character.get(player);

        if (character == null) return;

        if (global) Mappet.states.from(this.global);
        character.getStates().from(this.player);

        player.inventory.clear();
        for (int i = 0, c = Math.min(inventory.size(), player.inventory.getSizeInventory()); i < c; i++)
            player.inventory.setInventorySlotContents(i, inventory.get(i).copy());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("Global", global.serializeNBT());
        tag.setTag("Player", player.serializeNBT());

        NBTTagList inventory = new NBTTagList();
        for (ItemStack stack : this.inventory) inventory.appendTag(stack.serializeNBT());
        tag.setTag("Inventory", inventory);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Global")) global.deserializeNBT(tag.getCompoundTag("Global"));
        if (tag.hasKey("Player")) player.deserializeNBT(tag.getCompoundTag("Player"));

        NBTTagList inventory = tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inventory.tagCount(); i++) this.inventory.add(new ItemStack(inventory.getCompoundTagAt(i)));
    }
}