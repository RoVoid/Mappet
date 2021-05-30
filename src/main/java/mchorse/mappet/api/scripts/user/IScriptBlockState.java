package mchorse.mappet.api.scripts.user;

/**
 * Scripted block state
 *
 * This interface represents a block state that can be used
 * to compare or place into the world
 */
public interface IScriptBlockState
{
    /**
     * Get meta value of this state (it will always be between 0 and 15)
     */
    public int getMeta();

    /**
     * Get block's ID like "minecraft:stone"
     */
    public String getBlockId();

    /**
     * Check whether given block state contains same block state
     */
    public boolean isSame(IScriptBlockState state);

    /**
     * Check whether given block state has the same block, but
     * not necessarily the same meta value
     */
    public boolean isSameBlock(IScriptBlockState state);
}