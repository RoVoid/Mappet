package mchorse.mappet.api.scripts.code.nbt;


import net.minecraft.nbt.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptNBTCompound {
    private final NBTTagCompound tag;

    public ScriptNBTCompound(NBTTagCompound tag) {
        this.tag = tag == null ? new NBTTagCompound() : tag;
    }

    @Deprecated
    public NBTTagCompound getNBTTagCompound() {
        return tag;
    }

    public NBTTagCompound asMinecraft() {
        return tag;
    }


    public boolean isCompound() {
        return true;
    }


    public boolean isList() {
        return false;
    }


    @Deprecated
    public String stringify() {
        return tag.toString();
    }


    public String toString() {
        return tag.toString();
    }


    public boolean isEmpty() {
        return tag.hasNoTags();
    }


    public int size() {
        return tag.getSize();
    }


    public void combine(Object nbt) {
        if (nbt instanceof ScriptNBTCompound) tag.merge(((ScriptNBTCompound) nbt).tag);
    }


    public boolean isSame(Object nbt) {
        return nbt instanceof ScriptNBTCompound && tag.equals(((ScriptNBTCompound) nbt).tag);
    }


    public boolean has(String key) {
        return tag.hasKey(key);
    }


    public void remove(String key) {
        tag.removeTag(key);
    }


    public Set<String> keys() {
        return tag.getKeySet();
    }


    public ScriptNBTCompound copy() {
        return new ScriptNBTCompound(tag.copy());
    }

    /* ScriptNBTCompound implementation */


    public byte getByte(String key) {
        return tag.getByte(key);
    }


    public void setByte(String key, byte value) {
        tag.setByte(key, value);
    }


    public short getShort(String key) {
        return tag.getShort(key);
    }


    public void setShort(String key, short value) {
        tag.setShort(key, value);
    }


    public int getInt(String key) {
        return tag.getInteger(key);
    }


    public void setInt(String key, int value) {
        tag.setInteger(key, value);
    }


    public long getLong(String key) {
        return tag.getLong(key);
    }


    public void setLong(String key, long value) {
        tag.setLong(key, value);
    }


    public float getFloat(String key) {
        return tag.getFloat(key);
    }


    public void setFloat(String key, float value) {
        tag.setFloat(key, value);
    }


    public double getDouble(String key) {
        return tag.getDouble(key);
    }


    public void setDouble(String key, double value) {
        tag.setDouble(key, value);
    }


    public String getString(String key) {
        return tag.getString(key);
    }


    public void setString(String key, String value) {
        tag.setString(key, value);
    }


    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }


    public void setBoolean(String key, boolean value) {
        tag.setBoolean(key, value);
    }


    public ScriptNBTCompound getCompound(String key) {
        return new ScriptNBTCompound(tag.getCompoundTag(key));
    }


    public void setCompound(String key, ScriptNBTCompound value) {
        tag.setTag(key, value.tag);
    }


    public ScriptNBTList getList(String key) {
        NBTBase tag = this.tag.getTag(key);
        return new ScriptNBTList(tag instanceof NBTTagList ? (NBTTagList) tag : new NBTTagList());
    }


    public void setList(String key, ScriptNBTList value) {
        tag.setTag(key, value.asMinecraft());
    }


    public boolean setNBT(String key, String nbt) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson("{data:" + nbt + "}");

            tag.setTag(key, tag.getTag("data"));

            return true;
        } catch (Exception ignored) {
        }

        return false;
    }


    public Object get(String key) {
        NBTBase tag = this.tag.getTag(key);

        if (tag instanceof NBTTagCompound) return new ScriptNBTCompound((NBTTagCompound) tag);
        if (tag instanceof NBTTagList) return new ScriptNBTList((NBTTagList) tag);
        if (tag instanceof NBTTagString) return getString(key);
        if (tag instanceof NBTTagInt) return getInt(key);
        if (tag instanceof NBTTagDouble) return getDouble(key);
        if (tag instanceof NBTTagFloat) return getFloat(key);
        if (tag instanceof NBTTagLong) return getLong(key);
        if (tag instanceof NBTTagShort) return getShort(key);
        if (tag instanceof NBTTagByte) return getByte(key);

        return null;
    }


    public boolean equals(ScriptNBTCompound compound) {
        return compound != null && tag.equals(compound.asMinecraft());
    }


    public void addCompound(String key) {
        tag.setTag(key, new NBTTagCompound());
    }


    public String dumpJSON() {
        String result = stringify().replaceAll("([a-zA-Z0-9_]+):", "\"$1\":");

        Matcher matcher = Pattern.compile("([0-9]+[bLsdf])|0b|1b").matcher(result);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            if (matcher.group(0).equals("0b")) {
                matcher.appendReplacement(buffer, "false");
            }
            else if (matcher.group(0).equals("1b")) {
                matcher.appendReplacement(buffer, "true");
            }
            else {
                matcher.appendReplacement(buffer, matcher.group(1).substring(0, matcher.group(1).length() - 1));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}