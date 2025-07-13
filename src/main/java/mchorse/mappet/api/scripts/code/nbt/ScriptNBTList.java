package mchorse.mappet.api.scripts.code.nbt;

import mchorse.mappet.api.scripts.user.nbt.INBT;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTList;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

public class ScriptNBTList implements INBTList {
    private final NBTTagList list;

    public ScriptNBTList(NBTTagList list) {
        this.list = list == null ? new NBTTagList() : list;
    }

    @Override
    @Deprecated
    public NBTTagList getNBTTagList() {
        return list;
    }

    @Override
    public NBTTagList asMinecraft() {
        return list;
    }

    @Override
    public boolean isCompound() {
        return false;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    @Deprecated
    public String stringify() {
        return list.toString();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean isEmpty() {
        return list.hasNoTags();
    }

    @Override
    public int size() {
        return list.tagCount();
    }

    @Override
    public INBT copy() {
        return new ScriptNBTList(list.copy());
    }

    @Override
    public void combine(INBT nbt) {
        if (!(nbt instanceof INBTList)) return;

        NBTTagList list = ((INBTList) nbt).asMinecraft();
        if (this.list.getTagType() != list.getTagType()) return;

        for (int i = 0; i < list.tagCount(); i++) {
            list.appendTag(list.get(i).copy());
        }
    }

    @Override
    public boolean isSame(INBT nbt) {
        return nbt instanceof INBTList && list.equals(((INBTList) nbt).asMinecraft());
    }

    /* INBTCompound implementation */

    @Override
    public boolean has(int index) {
        return index >= 0 && index < size();
    }

    @Override
    public void remove(int index) {
        list.removeTag(index);
    }

    @Override
    public byte getByte(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_BYTE ? ((NBTPrimitive) base).getByte() : (byte) 0;
    }

    @Override
    public void setByte(int index, byte value) {
        list.set(index, new NBTTagByte(value));
    }

    @Override
    public void addByte(byte value) {
        list.appendTag(new NBTTagByte(value));
    }

    @Override
    public short getShort(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_SHORT ? ((NBTPrimitive) base).getShort() : (short) 0;
    }

    @Override
    public void setShort(int index, short value) {
        list.set(index, new NBTTagShort(value));
    }

    @Override
    public void addShort(short value) {
        list.appendTag(new NBTTagShort(value));
    }

    @Override
    public int getInt(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_INT ? ((NBTPrimitive) base).getInt() : 0;
    }

    @Override
    public void setInt(int index, int value) {
        list.set(index, new NBTTagInt(value));
    }

    @Override
    public void addInt(int value) {
        list.appendTag(new NBTTagInt(value));
    }

    @Override
    public long getLong(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_LONG ? ((NBTPrimitive) base).getLong() : 0;
    }

    @Override
    public void setLong(int index, long value) {
        list.set(index, new NBTTagLong(value));
    }

    @Override
    public void addLong(long value) {
        list.appendTag(new NBTTagLong(value));
    }

    @Override
    public float getFloat(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_FLOAT ? ((NBTPrimitive) base).getFloat() : 0;
    }

    @Override
    public void setFloat(int index, float value) {
        list.set(index, new NBTTagFloat(value));
    }

    @Override
    public void addFloat(float value) {
        list.appendTag(new NBTTagFloat(value));
    }

    @Override
    public double getDouble(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_DOUBLE ? ((NBTPrimitive) base).getDouble() : 0;
    }

    @Override
    public void setDouble(int index, double value) {
        list.set(index, new NBTTagDouble(value));
    }

    @Override
    public void addDouble(double value) {
        list.appendTag(new NBTTagDouble(value));
    }

    @Override
    public String getString(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_STRING ? ((NBTTagString) base).getString() : "";
    }

    @Override
    public void setString(int index, String value) {
        list.set(index, new NBTTagString(value));
    }

    @Override
    public void addString(String value) {
        list.appendTag(new NBTTagString(value));
    }

    @Override
    public boolean getBoolean(int index) {
        NBTBase base = list.get(index);
        return base.getId() == Constants.NBT.TAG_BYTE && ((NBTPrimitive) base).getByte() != 0;
    }

    @Override
    public void setBoolean(int index, boolean value) {
        list.set(index, new NBTTagByte(value ? (byte) 1 : (byte) 0));
    }

    @Override
    public void addBoolean(boolean value) {
        list.appendTag(new NBTTagByte(value ? (byte) 1 : (byte) 0));
    }

    @Override
    public INBTCompound getCompound(int index) {
        return new ScriptNBTCompound(list.getCompoundTagAt(index));
    }

    @Override
    public void setCompound(int index, INBTCompound value) {
        list.set(index, value.asMinecraft());
    }

    @Override
    public void addCompound(INBTCompound value) {
        list.appendTag(value.asMinecraft());
    }

    @Override
    public INBTList getList(int index) {
        NBTBase base = list.get(index);
        return new ScriptNBTList(base.getId() == Constants.NBT.TAG_LIST ? (NBTTagList) base : null);
    }

    @Override
    public void setList(int index, INBTList value) {
        list.set(index, value.asMinecraft());
    }

    @Override
    public void addList(INBTList value) {
        list.appendTag(value.asMinecraft());
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) array[i] = list.get(i);
        return array;
    }
}