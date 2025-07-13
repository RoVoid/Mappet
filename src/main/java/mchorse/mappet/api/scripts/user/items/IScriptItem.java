package mchorse.mappet.api.scripts.user.items;

import net.minecraft.item.Item;

/**
 * This interface represents an item
 */
public interface IScriptItem {
    /**
     * Use {@link #asMinecraft()} instead
     *
     * @deprecated
     */
    @Deprecated
    Item getMinecraftItem();

    /**
     * Get Minecraft item instance
     * <p><b>BEWARE:</b> You need to know the MCP mappings to directly call methods on this instance!</p>
     */
    Item asMinecraft();

    /**
     * Get item's ID like "minecraft:stick" or "minecraft:diamond_hoe"
     */
    String getId();

    /**
     * Check whether given item is same as this one
     */
    boolean isSame(IScriptItem item);
}