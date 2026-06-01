package mchorse.mappet.api.scripts.code.nbt;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

public class ScriptNBTList {
    private final NBTTagList list;

    public ScriptNBTList(NBTTagList list) {
        this.list = list == null ? new NBTTagList() : list;
    }

    @Deprecated
    public NBTTagList getNBTTagList() {
        return list;
    }

    public NBTTagList asMinecraft() {
        return list;
    }

    public boolean isCompound() {
        return false;
    }

    public boolean isList() {
        return true;
    }

    @Deprecated
    public String stringify() {
        return list.toString();
    }


    public String toString() {
        return list.toString();
    }


    public boolean isEmpty() {
        return list.hasNoTags();
    }


    public int size() {
        return list.tagCount();
    }


    public Object copy() {
        return new ScriptNBTList(list.copy());
    }


    public void combine(Object nbt) {
        if (!(nbt instanceof ScriptNBTList)) return;

        NBTTagList list = ((ScriptNBTList) nbt).asMinecraft();
        if (this.list.getTagType() != list.getTagType()) return;

        for (int i = 0; i < list.tagCount(); i++) {
            list.appendTag(list.get(i).copy());
        }
    }


    public boolean isSame(Object nbt) {
        return nbt instanceof ScriptNBTList && list.equals(((ScriptNBTList) nbt).asMinecraft());
    }

    /* ScriptNBTCompound implementation */


    public boolean has(int index) {
        return index >= 0 && index < size();
    }


    public void remove(int index) {
        list.removeTag(index);
    }


    public byte getByte(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_BYTE ? ((NBTPrimitive) base).getByte() : (byte) 0;
    }


    public void setByte(int index, byte value) {
        list.set(index, new NBTTagByte(value));
    }


    public void addByte(byte value) {
        list.appendTag(new NBTTagByte(value));
    }


    public short getShort(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_SHORT ? ((NBTPrimitive) base).getShort() : (short) 0;
    }


    public void setShort(int index, short value) {
        list.set(index, new NBTTagShort(value));
    }


    public void addShort(short value) {
        list.appendTag(new NBTTagShort(value));
    }


    public int getInt(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_INT ? ((NBTPrimitive) base).getInt() : 0;
    }


    public void setInt(int index, int value) {
        list.set(index, new NBTTagInt(value));
    }


    public void addInt(int value) {
        list.appendTag(new NBTTagInt(value));
    }


    public long getLong(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_LONG ? ((NBTPrimitive) base).getLong() : 0;
    }


    public void setLong(int index, long value) {
        list.set(index, new NBTTagLong(value));
    }


    public void addLong(long value) {
        list.appendTag(new NBTTagLong(value));
    }


    public float getFloat(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_FLOAT ? ((NBTPrimitive) base).getFloat() : 0;
    }


    public void setFloat(int index, float value) {
        list.set(index, new NBTTagFloat(value));
    }


    public void addFloat(float value) {
        list.appendTag(new NBTTagFloat(value));
    }


    public double getDouble(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_DOUBLE ? ((NBTPrimitive) base).getDouble() : 0;
    }


    public void setDouble(int index, double value) {
        list.set(index, new NBTTagDouble(value));
    }


    public void addDouble(double value) {
        list.appendTag(new NBTTagDouble(value));
    }


    public String getString(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_STRING ? ((NBTTagString) base).getString() : "";
    }


    public void setString(int index, String value) {
        list.set(index, new NBTTagString(value));
    }


    public void addString(String value) {
        list.appendTag(new NBTTagString(value));
    }


    public boolean getBoolean(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_BYTE && ((NBTPrimitive) base).getByte() != 0;
    }


    public void setBoolean(int index, boolean value) {
        list.set(index, new NBTTagByte(value ? (byte) 1 : (byte) 0));
    }


    public void addBoolean(boolean value) {
        list.appendTag(new NBTTagByte(value ? (byte) 1 : (byte) 0));
    }


    public ScriptNBTCompound getCompound(int index) {
        return new ScriptNBTCompound(list.getCompoundTagAt(index));
    }


    public void setCompound(int index, ScriptNBTCompound value) {
        list.set(index, value.asMinecraft());
    }


    public void addCompound(ScriptNBTCompound value) {
        list.appendTag(value.asMinecraft());
    }


    public ScriptNBTList getList(int index) {
        NBTBase base = list.get(index);
        return new ScriptNBTList(base.getId() == Constants.NBT.TAG_LIST ? (NBTTagList) base : null);
    }


    public void setList(int index, ScriptNBTList value) {
        list.set(index, value.asMinecraft());
    }


    public void addList(ScriptNBTList value) {
        list.appendTag(value.asMinecraft());
    }


    public Object[] toArray() {
        Object[] array = new Object[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) array[i] = list.get(i);
        return array;
    }
}