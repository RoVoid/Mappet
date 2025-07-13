package mchorse.mappet.api.scripts.user.nbt;

/**
 * Common interface for NBT data types.
 */
public interface INBT {
    /**
     * Check whether this NBT data is an NBT compound.
     */
    boolean isCompound();

    /**
     * Check whether this NBT data is an NBT list.
     */
    boolean isList();

    /**
     * Use {@link #toString()}
     * @deprecated
     */
    String stringify();

    /**
     * Convert this NBT structure to string.
     */
    String toString();

    /**
     * Check whether this NBT tag is empty.
     */
    boolean isEmpty();

    /**
     * Get the size (amount of elements) in this NBT tag.
     */
    int size();

    /**
     * Create a copy of this NBT tag.
     */
    INBT copy();

    /**
     * Add given NBT data's values on top of this one.
     */
    void combine(INBT nbt);

    /**
     * Check whether given NBT tag is same as this one.
     */
    boolean isSame(INBT nbt);
}