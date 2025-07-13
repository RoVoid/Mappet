package mchorse.mappet.api.scripts.code.items;

import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.user.items.IScriptItem;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptItemStack implements IScriptItemStack {
    public static final ScriptItemStack EMPTY = new ScriptItemStack(ItemStack.EMPTY);

    private static final String CAN_DESTROY = "CanDestroy";
    private static final String CAN_PLACE_ON = "CanPlaceOn";

    private final ItemStack stack;
    private IScriptItem item;

    public static IScriptItemStack create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY;
        }

        return new ScriptItemStack(stack);
    }

    private ScriptItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    @Deprecated
    public ItemStack getMinecraftItemStack() {
        return stack;
    }

    @Override
    public ItemStack asMinecraft() {
        return stack;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public IScriptItemStack copy() {
        return new ScriptItemStack(asMinecraft().copy());
    }

    @Override
    public IScriptItem getItem() {
        if (item == null) item = new ScriptItem(stack.getItem());
        return item;
    }

    @Override
    public int getMaxCount() {
        return stack.getMaxStackSize();
    }

    @Override
    public int getCount() {
        return stack.getCount();
    }

    @Override
    public void setCount(int count) {
        stack.setCount(count);
    }

    @Override
    public int getMeta() {
        return stack.getMetadata();
    }

    @Override
    public void setMeta(int meta) {
        stack.setItemDamage(meta);
    }

    @Override
    public boolean hasData() {
        return stack.hasTagCompound();
    }

    @Override
    public INBTCompound getData() {
        return new ScriptNBTCompound(stack.getTagCompound());
    }

    @Override
    public void setData(INBTCompound tag) {
        stack.setTagCompound(tag.getNBTTagCompound());
    }

    @Override
    public INBTCompound serialize() {
        return new ScriptNBTCompound(stack.serializeNBT());
    }

    @Override
    public String getDisplayName() {
        return stack.getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        stack.setStackDisplayName(name);
    }

    private NBTTagList getLoreNBTList() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return null;


        if (!tag.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
            tag.setTag("display", new NBTTagCompound());
        }

        NBTTagCompound display = tag.getCompoundTag("display");

        if (!display.hasKey("Lore", Constants.NBT.TAG_LIST)) {
            display.setTag("Lore", new NBTTagList());
        }

        return display.getTagList("Lore", Constants.NBT.TAG_STRING);
    }

    @Override
    public String getLore(int index) {
        NBTTagList list = getLoreNBTList();

        if (list != null && index < list.tagCount()) {
            return list.getStringTagAt(index);
        }

        throw new IllegalStateException("Lore index out of bounds, or no lore exists.");
    }

    @Override
    public List<String> getLoreList() {
        NBTTagList lore = getLoreNBTList();
        if (lore == null) return Collections.emptyList();

        List<String> loreList = new ArrayList<>();

        for (int i = 0; i < lore.tagCount(); i++) {
            loreList.add(lore.getStringTagAt(i));
        }

        return loreList;
    }

    @Override
    public void setLore(int index, String string) {
        NBTTagList lore = getLoreNBTList();

        if (lore != null && index >= 0 && index < lore.tagCount()) {
            lore.set(index, new NBTTagString(string));
        } else {
            throw new IllegalStateException("Lore index out of bounds, or no lore exists.");
        }
    }

    @Override
    public void addLore(String string) {
        NBTTagList lore = getLoreNBTList();
        if (lore != null) lore.appendTag(new NBTTagString(string));
    }

    @Override
    public void clearAllLores() {
        NBTTagList lore = getLoreNBTList();

        if (lore == null) return;
        while (lore.tagCount() > 0) {
            lore.removeTag(lore.tagCount() - 1);
        }
    }

    @Override
    public void clearLore(int index) {
        NBTTagList lore = getLoreNBTList();

        if (lore != null && index >= 0 && index < lore.tagCount()) {
            lore.removeTag(index);
        } else {
            throw new IllegalStateException("Lore index out of bounds, or no lore exists.");
        }
    }

    @Override
    public void clearAllEnchantments() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) tag.removeTag("ench");
    }

    @Override
    public List<String> getCanDestroyBlocks() {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.hasKey(CAN_DESTROY, Constants.NBT.TAG_LIST)) {
            return Collections.emptyList();
        }

        List<String> canDestroyBlocks = new ArrayList<>();
        NBTTagList list = tag.getTagList(CAN_DESTROY, Constants.NBT.TAG_STRING);

        for (int i = 0; i < list.tagCount(); i++) {
            canDestroyBlocks.add(list.getStringTagAt(i));
        }

        return canDestroyBlocks;
    }

    @Override
    public void addCanDestroyBlock(String block) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        NBTTagList canDestroyList;
        if (tag.hasKey(CAN_DESTROY, Constants.NBT.TAG_LIST)) {
            canDestroyList = tag.getTagList(CAN_DESTROY, Constants.NBT.TAG_STRING);
        } else {
            canDestroyList = new NBTTagList();
            tag.setTag(CAN_DESTROY, canDestroyList);
        }

        for (int i = 0; i < canDestroyList.tagCount(); i++) {
            if (canDestroyList.getStringTagAt(i).equals(block)) {
                return; // If the block is already in the list, do not add it again.
            }
        }

        canDestroyList.appendTag(new NBTTagString(block));
    }

    @Override
    public void clearAllCanDestroyBlocks() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) tag.removeTag(CAN_DESTROY);
    }

    @Override
    public void clearCanDestroyBlock(String block) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) return;

        NBTTagList canPlaceOn = tag.getTagList(CAN_DESTROY, Constants.NBT.TAG_STRING);
        NBTTagList newCanPlaceOn = new NBTTagList();

        for (int i = 0; i < canPlaceOn.tagCount(); i++) {
            if (!canPlaceOn.getStringTagAt(i).equals(block)) {
                newCanPlaceOn.appendTag(canPlaceOn.get(i));
            }
        }

        tag.setTag(CAN_DESTROY, newCanPlaceOn);
    }

    @Override
    public List<String> getCanPlaceOnBlocks() {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.hasKey(CAN_PLACE_ON, Constants.NBT.TAG_LIST)) {
            return Collections.emptyList();
        }

        List<String> canPlaceOn = new ArrayList<>();
        NBTTagList list = tag.getTagList(CAN_PLACE_ON, Constants.NBT.TAG_STRING);

        for (int i = 0; i < list.tagCount(); i++) canPlaceOn.add(list.getStringTagAt(i));

        return canPlaceOn;
    }

    @Override
    public void addCanPlaceOnBlock(String block) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (!tag.hasKey(CAN_PLACE_ON, Constants.NBT.TAG_LIST)) {
            tag.setTag(CAN_PLACE_ON, new NBTTagList());
        }

        tag.getTagList(CAN_PLACE_ON, Constants.NBT.TAG_STRING).appendTag(new NBTTagString(block));
    }

    @Override
    public void clearAllCanPlaceOnBlocks() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) tag.removeTag(CAN_PLACE_ON);
    }

    @Override
    public void clearCanPlaceOnBlock(String block) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;

        NBTTagList canPlaceOn = tag.getTagList(CAN_PLACE_ON, Constants.NBT.TAG_STRING);
        NBTTagList newCanPlaceOn = new NBTTagList();

        for (int i = 0; i < canPlaceOn.tagCount(); i++) {
            if (!canPlaceOn.getStringTagAt(i).equals(block)) {
                newCanPlaceOn.appendTag(canPlaceOn.get(i));
            }
        }

        tag.setTag(CAN_PLACE_ON, newCanPlaceOn);
    }

    @Override
    public int getRepairCost() {
        return stack.getRepairCost();
    }

    @Override
    public void setRepairCost(int cost) {
        stack.setRepairCost(cost);
    }

    @Override
    public boolean isUnbreakable() {
        return !stack.isItemStackDamageable();
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) tag.setBoolean("Unbreakable", unbreakable);
    }

    @Override
    public void add(int amount) {
        int newCount = stack.getCount() + amount;

        if (newCount <= 0) stack.shrink(stack.getCount());
        else stack.setCount(newCount);
    }

    @Override
    public boolean equals(ScriptItemStack other) {
        return stack.isItemEqual(other.stack) && ItemStack.areItemStackTagsEqual(stack, other.stack);
    }
}