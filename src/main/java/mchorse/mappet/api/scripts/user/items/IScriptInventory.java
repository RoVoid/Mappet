package mchorse.mappet.api.scripts.user.items;

import mchorse.mappet.api.scripts.user.entities.IScriptPlayer;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import net.minecraft.inventory.IInventory;

/**
 * This interface represents an inventory.
 *
 * <p>See {@link IScriptPlayer#getInventory()} and
 * {@link IScriptWorld#getInventory(int, int, int)} for ways to get
 * inventories from player and world, respectively.</p>
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        // Get player's inventory
 *        if (c.getSubject().isPlayer())
 *        {
 *            var inventory = c.getSubject().getInventory();
 *
 *            // Do something with player's inventory
 *        }
 *
 *        // Get chest's inventory in the world
 *        if (c.getWorld().hasInventory(214, 4, 512))
 *        {
 *            var inventory = c.getWorld().getInventory(214, 4, 512);
 *
 *            // Do something with chest's inventory
 *        }
 *    }
 * }</pre>
 */
public interface IScriptInventory {
    /**
     * Use {@link #asMinecraft()} instead
     *
     * @deprecated
     */
    @Deprecated
    IInventory getMinecraftInventory();

    /**
     * Get Minecraft inventory instance
     * <p><b>BEWARE:</b> You need to know the MCP mappings to directly call methods on this instance!</p>
     */
    IInventory asMinecraft();

    /**
     * Check whether this inventory is empty.
     */
    boolean isEmpty();

    /**
     * Return the maximum amount of item stacks in this inventory.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getInventory();
     *    var item = mappet.createItem("minecraft:stick");
     *
     *    for (var i = 0; i < inventory.size(); i++)
     *    {
     *        // We do a little bit of trolling
     *        inventory.setStack(i, item);
     *    }
     * }</pre>
     */
    int size();

    /**
     * Get stack in slot at given index.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getInventory();
     *    var first = inventory.getStack(0);
     *
     *    if (first.isEmpty())
     *    {
     *        // Give a stick into first player's hotbar slot
     *        inventory.setStack(0, mappet.createItem("minecraft:stick"));
     *    }
     * }</pre>
     *
     * @return an item stack at given index
     */
    IScriptItemStack getStack(int index);

    /**
     * Remove a stack at given index
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getInventory();
     *    var first = inventory.removeStack(0);
     *
     *    if (first.isEmpty())
     *    {
     *        c.getSubject().send("Oh... you had nothing...");
     *    }
     *    else
     *    {
     *        c.getSubject().send("Ha-ha, I stole your " + first.getItem().getId());
     *    }
     * }</pre>
     *
     * @return removed item stack
     */
    IScriptItemStack removeStack(int index);

    /**
     * Replace given stack at index.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getInventory();
     *
     *    inventory.setStack(4, mappet.createItem("minecraft:diamond_sword"));
     * }</pre>
     */
    void setStack(int index, IScriptItemStack stack);

    /**
     * Empty the inventory.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    c.getSubject().getInventory().clear();
     * }</pre>
     */
    void clear();

    /* Basic inventory */

    /**
     * Get basic inventory's name. This works only for inventories that support
     * naming, like chests.
     */
    String getName();

    /**
     * Whether this inventory has a name. This works only for inventories that
     * support naming, like chests.
     */
    boolean hasCustomName();

    /**
     * Set basic inventory's name. This works only for inventories that
     * support naming, like chests.
     */
    void setName(String name);
}