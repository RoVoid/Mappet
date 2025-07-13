package mchorse.mappet.api.scripts.code.nbt;

import mchorse.mappet.api.scripts.user.nbt.INBT;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTList;
import net.minecraft.nbt.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptNBTCompound implements INBTCompound {
    private final NBTTagCompound tag;

    public ScriptNBTCompound(NBTTagCompound tag) {
        this.tag = tag == null ? new NBTTagCompound() : tag;
    }

    @Override
    @Deprecated
    public NBTTagCompound getNBTTagCompound() {
        return tag;
    }

    @Override
    public NBTTagCompound asMinecraft() {
        return tag;
    }

    @Override
    public boolean isCompound() {
        return true;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    @Deprecated
    public String stringify() {
        return tag.toString();
    }

    @Override
    public String toString() {
        return tag.toString();
    }

    @Override
    public boolean isEmpty() {
        return tag.hasNoTags();
    }

    @Override
    public int size() {
        return tag.getSize();
    }

    @Override
    public void combine(INBT nbt) {
        if (nbt instanceof ScriptNBTCompound) tag.merge(((ScriptNBTCompound) nbt).tag);
    }

    @Override
    public boolean isSame(INBT nbt) {
        return nbt instanceof ScriptNBTCompound && tag.equals(((ScriptNBTCompound) nbt).tag);
    }

    @Override
    public boolean has(String key) {
        return tag.hasKey(key);
    }

    @Override
    public void remove(String key) {
        tag.removeTag(key);
    }

    @Override
    public Set<String> keys() {
        return tag.getKeySet();
    }

    @Override
    public INBTCompound copy() {
        return new ScriptNBTCompound(tag.copy());
    }

    /* INBTCompound implementation */

    @Override
    public byte getByte(String key) {
        return tag.getByte(key);
    }

    @Override
    public void setByte(String key, byte value) {
        tag.setByte(key, value);
    }

    @Override
    public short getShort(String key) {
        return tag.getShort(key);
    }

    @Override
    public void setShort(String key, short value) {
        tag.setShort(key, value);
    }

    @Override
    public int getInt(String key) {
        return tag.getInteger(key);
    }

    @Override
    public void setInt(String key, int value) {
        tag.setInteger(key, value);
    }

    @Override
    public long getLong(String key) {
        return tag.getLong(key);
    }

    @Override
    public void setLong(String key, long value) {
        tag.setLong(key, value);
    }

    @Override
    public float getFloat(String key) {
        return tag.getFloat(key);
    }

    @Override
    public void setFloat(String key, float value) {
        tag.setFloat(key, value);
    }

    @Override
    public double getDouble(String key) {
        return tag.getDouble(key);
    }

    @Override
    public void setDouble(String key, double value) {
        tag.setDouble(key, value);
    }

    @Override
    public String getString(String key) {
        return tag.getString(key);
    }

    @Override
    public void setString(String key, String value) {
        tag.setString(key, value);
    }

    @Override
    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        tag.setBoolean(key, value);
    }

    @Override
    public INBTCompound getCompound(String key) {
        return new ScriptNBTCompound(tag.getCompoundTag(key));
    }

    @Override
    public void setCompound(String key, INBTCompound value) {
        tag.setTag(key, ((ScriptNBTCompound) value).tag);
    }

    @Override
    public INBTList getList(String key) {
        NBTBase tag = this.tag.getTag(key);
        return new ScriptNBTList(tag instanceof NBTTagList ? (NBTTagList) tag : new NBTTagList());
    }

    @Override
    public void setList(String key, INBTList value) {
        tag.setTag(key, value.asMinecraft());
    }

    @Override
    public boolean setNBT(String key, String nbt) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson("{data:" + nbt + "}");

            tag.setTag(key, tag.getTag("data"));

            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public Object get(String key) {
        NBTBase tag = this.tag.getTag(key);

        if (tag instanceof NBTTagCompound) return new ScriptNBTCompound((NBTTagCompound) tag);
        else if (tag instanceof NBTTagList) return new ScriptNBTList((NBTTagList) tag);
        else if (tag instanceof NBTTagString) return getString(key);
        else if (tag instanceof NBTTagInt) return getInt(key);
        else if (tag instanceof NBTTagDouble) return getDouble(key);
        else if (tag instanceof NBTTagFloat) return getFloat(key);
        else if (tag instanceof NBTTagLong) return getLong(key);
        else if (tag instanceof NBTTagShort) return getShort(key);
        else if (tag instanceof NBTTagByte) return getByte(key);

        return null;
    }

    @Override
    public boolean equals(INBTCompound compound) {
        return compound != null && tag.equals(compound.asMinecraft());
    }

    @Override
    public void addCompound(String key) {
        tag.setTag(key, new NBTTagCompound());
    }

    @Override
    public String dumpJSON() {
        String result = stringify().replaceAll("([a-zA-Z0-9_]+):", "\"$1\":");

        Matcher matcher = Pattern.compile("([0-9]+[bLsdf])|0b|1b").matcher(result);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            if (matcher.group(0).equals("0b")) {
                matcher.appendReplacement(buffer, "false");
            } else if (matcher.group(0).equals("1b")) {
                matcher.appendReplacement(buffer, "true");
            } else {
                matcher.appendReplacement(buffer, matcher.group(1).substring(0, matcher.group(1).length() - 1));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}