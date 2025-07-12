package mchorse.mappet.api.scripts.user.blocks;

import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import net.minecraft.block.state.IBlockState;

/**
 * Block State
 * <p> CREATE: {@link mchorse.mappet.api.scripts.user.IScriptFactory#createBlock(String, int)}
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        var block = c.getWorld().getBlock(214, 3, 511)
 *        if (block.getId("minecraft:stone") && block.getMeta() === 0)
 *        {
 *            c.send("Block at (214, 3, 511) is indeed stone!");
 *        }
 *    }
 * }</pre>
 */
public interface IScriptBlockState {
    /**
     * @deprecated Use {@link #asMinecraft()} instead
     */
    @Deprecated
    IBlockState getMinecraftBlockState();

    /**
     * Get Minecraft block state instance
     *
     * <p style="color:yellow"><b>BEWARE:</b> You need to know the MCP mappings to directly call methods on this instance!</p>
     */
    IBlockState asMinecraft();

    /**
     * @deprecated Use {@link #getId()} instead
     */
    @Deprecated
    String getBlockId();

    /**
     * Get block's ID like <code>minecraft:stone</code>
     *
     * <pre>{@code
     *    var block = c.getWorld().getBlock(214, 3, 511);
     *    c.send("Block ID is " + block.getBlockId());
     * }</pre>
     */
    String getId();

    /**
     * Get meta of this state (it will always be between 0 and 15)
     *
     * <pre>{@code
     *    var andesite = mappet.createBlockState("minecraft:stone", 5);
     *    c.send("Meta equals " + andesite.getMeta()); // Result: "Meta equals 5"
     * }</pre>
     */
    int getMeta();

    /**
     * Compare this block state with another
     *
     * <pre>{@code
     *    var stone = mappet.createBlockState("minecraft:stone");
     *    var andesite = mappet.createBlockState("minecraft:stone", 5);
     *    c.send(stone.isSame(andesite)); // Result: "false"
     * }</pre>
     */
    boolean isSame(IScriptBlockState state);

    /**
     * Compare this block state with another, but not necessarily the same meta
     *
     * <pre>{@code
     *    var stone = mappet.createBlockState("minecraft:stone");
     *    var andesite = mappet.createBlockState("minecraft:stone", 5);
     *    var air = mappet.createBlockState("minecraft:air");
     *    c.send(stone.isSameBlock(andesite)); // Result: "true"
     *    c.send(stone.isSameBlock(air)); // Result: "false"
     * }</pre>
     */
    boolean isSameBlock(IScriptBlockState state);

    /**
     * Returns true if the block state occupies the full block and is not transparent
     */
    boolean isOpaque();

    /**
     * Checks if the block state has collision boxes at a given position
     * <p>Collision shape may differ depending on the world and coordinates</p>
     */
    boolean hasCollision(IScriptWorld world, int x, int y, int z);

    boolean hasCollision(IScriptWorld world, ScriptVector vector);

    /**
     * Checks if this block state is air
     */
    boolean isAir();
}