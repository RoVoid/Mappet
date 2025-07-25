package mchorse.mappet.api.scripts.user.nbt;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

/**
 * Interface that represents an NBT compound tag
 */
public interface INBTCompound extends INBT {
    /**
     * Use {@link #asMinecraft()} instead
     * @deprecated
     */
    @Deprecated
    NBTTagCompound getNBTTagCompound();

    /**
     * Get raw NBT tag compound
     * <b>BEWARE:</b> you need to know the MCP mappings to directly call methods on this instance!
     */
    NBTTagCompound asMinecraft();

    /**
     * Check whether this NBT compound has a value by given key.
     */
    boolean has(String key);

    /**
     * Remove a value by given key.
     */
    void remove(String key);

    /**
     * Get all the keys.
     */
    Set<String> keys();

    /* Assignment/query */

    /**
     * Get byte (8-bit integer) value by given key.
     */
    byte getByte(String key);

    /**
     * Set byte (8-bit integer) value by given key.
     */
    void setByte(String key, byte value);

    /**
     * Get short (16-bit integer) value by given key.
     */
    short getShort(String key);

    /**
     * Set short (16-bit integer) value by given key.
     */
    void setShort(String key, short value);

    /**
     * Get integer (32-bit integer) value by given key.
     */
    int getInt(String key);

    /**
     * Set integer (32-bit integer) value by given key.
     */
    void setInt(String key, int value);

    /**
     * Get long (64-bit integer) value by given key.
     */
    long getLong(String key);

    /**
     * Set long (64-bit integer) value by given key.
     */
    void setLong(String key, long value);

    /**
     * Get float (32-bit floating point number) value by given key.
     */
    float getFloat(String key);

    /**
     * Set float (32-bit floating point number) value by given key.
     */
    void setFloat(String key, float value);

    /**
     * Get double (64-bit floating point number) value by given key.
     */
    double getDouble(String key);

    /**
     * Set double (64-bit floating point number) value by given key.
     */
    void setDouble(String key, double value);

    /**
     * Get string value by given key.
     */
    String getString(String key);

    /**
     * Set string value by given key.
     */
    void setString(String key, String value);

    /**
     * Get boolean (true or false) value by given key.
     */
    boolean getBoolean(String key);

    /**
     * Set boolean (true or false) value by given key.
     */
    void setBoolean(String key, boolean value);

    /**
     * Get NBT compound by given key.
     */
    INBTCompound getCompound(String key);

    /**
     * Set NBT compound by given key.
     */
    void setCompound(String key, INBTCompound value);

    /**
     * Get NBT list by given key.
     */
    INBTList getList(String key);

    /**
     * Set NBT list by given key.
     */
    void setList(String key, INBTList value);

    /**
     * Set arbitrary NBT.
     *
     * <pre>{@code
     *    var compound = mappet.createCompound();
     *
     *    compound.setNBT("stack", '{id:"minecraft:diamond",Count:64b}');
     *
     *    // {stack:{id:"minecraft:diamond",Count:64b}}
     *    print(compound.stringify());
     * }</pre>
     *
     * @return Whether given NBT code was successfully inserted.
     */
    boolean setNBT(String key, String nbt);

    /**
     * Get the value of in this compound by given key of the raw type. Following
     * value types are possible (depending on whatever was found in the compound):
     * INBTCompound, INBTList, String, Double, Long, Float, Int, Short, Byte, or
     * null if the value is absent by given key.
     *
     * <pre>{@code
     *     var tag = mappet.createCompound("{id:\"minecraft:diamond_hoe\",Count:1b}");
     *
     *     c.send(tag.get("id"));
     *     c.send(tag.get("Count"));
     * }</pre>
     *
     * @param key the key of the value
     * @return the value found by that key or null
     */
    Object get(String key);

    /**
     * Check if this compound is equal to given compound (order of keys doesn't matter).
     *
     * <pre>{@code
     *     var tag1 = mappet.createCompound("{id:\"minecraft:diamond_hoe\",Count:1b}");
     *     var tag2 = mappet.createCompound("{Count:1b,id:\"minecraft:diamond_hoe\"}");
     *
     *     c.send(tag1.equals(tag2));
     * }</pre>
     *
     * @param compound the compound to compare with
     * @return whether this compound is equal to the given compound
     */
    boolean equals(INBTCompound compound);

    /**
     * Adds a new compound to this compound.
     *
     * <pre>{@code
     *    var tag = mappet.createCompound();
     *    tag.addCompound("compound");
     *    tag.get("compound").setString("x", "123")
     *    c.send(tag) //{compound:{x:"123"}}
     * }</pre>
     */
    void addCompound(String key);

    /**
     * Dumps to a JSON String.
     * It first replaces the key-value colons with the proper JSON format.
     * Then, it creates a pattern to match the desired numeric literals and boolean values.
     * It checks if the current matched pattern is "0b" or "1b"
     * and replaces them with "false" and "true" respectively.
     * For the other cases, it removes the last character from the matched pattern,
     * effectively removing the type literal.
     *
     * <pre>{@code
     *   var tag = mappet.createCompound("{id:\"minecraft:diamond_hoe\",Count:1b}");
     *   c.send(tag.dumpJSON());
     * }</pre>
     */
    String dumpJSON();
}