package mchorse.mappet.api.scripts.user.items;

import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * This interface represents an item stack
 */
public interface IScriptItemStack
{
    /**
     * Use {@link #asMinecraft()} instead
     *
     * @deprecated
     */
    @Deprecated
    ItemStack getMinecraftItemStack();

    /**
     * Get Minecraft item stack instance
     * <p><b>BEWARE:</b> You need to know the MCP mappings to directly call methods on this instance!</p>
     */
    ItemStack asMinecraft();

    /**
     * Whether this item is empty.
     */
     boolean isEmpty();

    /**
     * Get item stack's item.
     */
     IScriptItem getItem();

    /**
     * Get a copy of item stack.
     */
     IScriptItemStack copy();

    /**
     * Get item stack's maximum size.
     */
     int getMaxCount();

    /**
     * Get item stack's count.
     */
     int getCount();

    /**
     * Set item stack's count.
     */
     void setCount(int count);

    /**
     * Get item stack's meta.
     */
     int getMeta();

    /**
     * Set item stack's meta.
     */
     void setMeta(int meta);

    /**
     * Check whether an item stack has an NBT compound tag.
     */
     boolean hasData();

    /**
     * Get item stack's NBT compound tag.
     */
     INBTCompound getData();

    /**
     * Replace item stack's NBT compound tag.
     */
     void setData(INBTCompound tag);

    /**
     * Serialize item stack to an NBT compound.
     */
     INBTCompound serialize();

    /**
     * Get display name of the item stack.
     */
     String getDisplayName();

    /**
     * Set display name of the item stack.
     */
     void setDisplayName(String name);

    /**
     * Get lore of the item stack.
     */
     String getLore(int index);

    /**
     * Get all lore lines of the item stack as a list.
     */
     List<String> getLoreList();

    /**
     * Set lore of the item stack.
     */
     void setLore(int index, String lore);

    /**
     * Add a lore line to the item stack.
     */
     void addLore(String lore);

    /**
     * Remove all lore lines from the item stack.
     */
     void clearAllLores();

    /**
     * Remove a lore line from the item stack.
     */
     void clearLore(int index);

    /**
     * Clear all enchantments from the item stack.
     */
     void clearAllEnchantments();

    /**
     * Get a list of all blocks the item stack can destroy.
     */
     List<String> getCanDestroyBlocks();

    /**
     * Add a block that the item stack can destroy.
     */
     void addCanDestroyBlock(String block);

    /**
     * Clear all blocks that the item stack can destroy.
     */
     void clearAllCanDestroyBlocks();

    /**
     * Clear a block that the item stack can destroy.
     */
     void clearCanDestroyBlock(String block);

    /**
     * Get a list of all blocks the item stack can place on.
     */
     List<String> getCanPlaceOnBlocks();

    /**
     * Add a block that the item stack can place on.
     */
     void addCanPlaceOnBlock(String block);

    /**
     * Clear all blocks that the item stack can place on.
     */
     void clearAllCanPlaceOnBlocks();

    /**
     * Clear a block that the item stack can place on.
     */
     void clearCanPlaceOnBlock(String block);

    /**
     * Get repair cost of the item stack.
     */
     int getRepairCost();

    /**
     * Set repair cost of the item stack.
     */
     void setRepairCost(int cost);

    /**
     * Check if an item stack is unbreakable.
     */
     boolean isUnbreakable();

    /**
     * Set whether an item stack is unbreakable or not.
     */
     void setUnbreakable(boolean unbreakable);

    /**
     * Add/remove more items to the stack.
     */
     void add(int amount);

    /**
     * Check if this item stack is equal to another item stack.
     */
     boolean equals(ScriptItemStack other);
}