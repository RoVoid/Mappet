package mchorse.mappet.api.scripts.user.blocks;

import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Tile Entity
 *
 * <p>This interface represents Minecraft tile entities, which are special
 * kind of entities that exist within blocks (like crafting tables, chests,
 * furnace, etc.)</p>
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        var te = c.getWorld().getTileEntity(371, 4, -100);
 *        if (te.getId() === "mappet:region")
 *        {
 *            var data = te.getData();
 *
 *            // Replace on enter trigger of the tile entity to /kill @s
 *            data.getCompound("Region").setNBT("OnEnter", '{Blocks:[{Command:"/kill @s",Type:"command"}]}');
 *            te.setData(data);
 *        }
 *    }
 * }</pre>
 */
public interface IScriptTileEntity {
    /**
     * @deprecated Use {@link #asMinecraft()} instead
     */
    TileEntity getMinecraftTileEntity();

    /**
     * Get Minecraft tile entity instance
     *
     * <p style="color:yellow"><b>BEWARE:</b> You need to know the MCP mappings to directly call methods on this instance!</p>
     */
    TileEntity asMinecraft();

    /**
     * Get tile entity's ID
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var te = c.getWorld().getTileEntity(-218, 101, 199)
     *     c.send(te.getId())
     * }
     * }</pre>
     */
    String getId();


    /**
     * Checks whether this tile entity is invalid (e.g., removed from the world or otherwise unavailable)
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var te = c.getWorld().getTileEntity(-218, 101, 199)
     *     c.send(te.isInvalid())
     * }
     * }</pre>
     */
    boolean isInvalid();

    /**
     * Returns a copy of this tile entity's NBT data
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var te = c.getWorld().getTileEntity(-218, 101, 199)
     *     c.send(te.getData())
     * }
     * }</pre>
     */
    INBTCompound getData();

    /**
     * Overwrites the NBT data of this tile entity
     * <p style="color:yellow"><b>WARNING:</b> incorrect use may corrupt the tile entity</p>
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var te = c.getWorld().getTileEntity(-218, 101, 199)
     *     var newData = mappet.createCompound('{CookTime:0,x:-218,BurnTime:0,y:101,z:199,Item:[],id:"minecraft:furnace",CookTimeTotal:0,Lock:""}')
     *     te.setData(newData)
     * }
     * }</pre>
     */
    void setData(INBTCompound compound);

    /**
     * Returns Forge's custom NBT compound where you can store arbitrary data
     * <p>Changes to the returned compound <b>directly affect the tile entity's data</b></p>
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var te = c.getWorld().getTileEntity(-218, 101, 199)
     *     var data = te.getTileData()
     *     data.setInt("count", 1); // affected the block
     *     c.send(te.getTileData())
     * }
     * }</pre>
     */
    INBTCompound getTileData();
}