package mchorse.mappet.api.scripts.user.nbt;

import net.minecraft.nbt.NBTTagList;

/**
 * Interface that represents an NBT list tag
 */
public interface INBTList extends INBT {
    /**
     * Use {@link #asMinecraft()} instead
     *
     * @deprecated
     */
    @Deprecated
    NBTTagList getNBTTagList();

    /**
     * Get a raw NBT tag list
     * <b>BEWARE:</b> you need to know the MCP mappings to directly call methods on this instance!
     */
    NBTTagList asMinecraft();

    /**
     * Check whether this list has an element at given index (instead of
     * checking manually for index to be within 0..size-1 range)
     */
    boolean has(int index);

    /**
     * Remove an element at given index
     */
    void remove(int index);

    /**
     * Get byte (8-bit integer) value at given index
     */
    byte getByte(int index);

    /**
     * Set byte (8-bit integer) value at given index
     */
    void setByte(int index, byte value);

    /**
     * Add byte (8-bit integer) value at the end of the list
     */
    void addByte(byte value);

    /**
     * Get short (16-bit integer) value at given index
     */
    short getShort(int index);

    /**
     * Set short (16-bit integer) value at given index
     */
    void setShort(int index, short value);

    /**
     * Add short (16-bit integer) value at the end of the list
     */
    void addShort(short value);

    /**
     * Get integer (32-bit integer) value at given index
     */
    int getInt(int index);

    /**
     * Set integer (32-bit integer) value at given index
     */
    void setInt(int index, int value);

    /**
     * Add integer (32-bit integer) value at the end of the list
     */
    void addInt(int value);

    /**
     * Get long (64-bit integer) value at given index
     */
    long getLong(int index);

    /**
     * Set long (64-bit integer) value at given index
     */
    void setLong(int index, long value);

    /**
     * Add long (64-bit integer) value at the end of the list
     */
    void addLong(long value);

    /**
     * Get float (32-bit floating point number) value at given index
     */
    float getFloat(int index);

    /**
     * Set float (32-bit floating point number) value at given index
     */
    void setFloat(int index, float value);

    /**
     * Add float (32-bit floating point number) value at the end of the list
     */
    void addFloat(float value);

    /**
     * Get double (64-bit floating point number) value at given index
     */
    double getDouble(int index);

    /**
     * Set double (64-bit floating point number) value at given index
     */
    void setDouble(int index, double value);

    /**
     * Add double (64-bit floating point number) value at the end of the list
     */
    void addDouble(double value);

    /**
     * Get string value at given index
     */
    String getString(int index);

    /**
     * Set string value at given index
     */
    void setString(int index, String value);

    /**
     * Add string value at the end of the list
     */
    void addString(String value);

    /**
     * Get boolean (true or false) value at given index
     */
    boolean getBoolean(int index);

    /**
     * Set boolean (true or false) value at given index
     */
    void setBoolean(int index, boolean value);

    /**
     * Add boolean (true or false) value at the end of the list
     */
    void addBoolean(boolean value);

    /**
     * Get NBT compound at given index
     */
    INBTCompound getCompound(int index);

    /**
     * Set NBT compound at given index
     */
    void setCompound(int index, INBTCompound value);

    /**
     * Add NBT compound at the end of the list
     */
    void addCompound(INBTCompound value);

    /**
     * Get NBT list at given index
     */
    INBTList getList(int index);

    /**
     * Set NBT list at given index
     */
    void setList(int index, INBTList value);

    /**
     * Add NBT list at the end of the list
     */
    void addList(INBTList value);

    /**
     * Turns a NBT list into a Java array.
     *
     * <pre>{@code
     *     var tag = mappet.createCompound("{id:[0,2,4]}");
     *
     *     c.send(tag.get("id").toArray()[1]); // 2
     * }</pre>
     *
     * @return an array of the list's elements
     */
    Object[] toArray();
}