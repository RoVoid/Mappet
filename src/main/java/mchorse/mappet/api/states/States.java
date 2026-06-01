package mchorse.mappet.api.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.events.StateChangedEvent;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class States implements INBTSerializable<NBTTagCompound> {
    protected final Map<String, Object> values = new HashMap<>();
    public States() {}

    public States(States other) {
        from(other);
    }

    public void from(States states) {
        from(states, true);
    }

    public void from(States states, boolean withPost) {
        if (states == null) return;
        values.clear();
        values.putAll(states.values);
        if (withPost) post(null, null, null);
    }

    public Map<String, Object> values() {
        return values;
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public boolean has(String key) {return values.containsKey(key);}

    public boolean equals(String key, Object otherValue) {
        Object value = values.get(key);
        if (value == null || otherValue == null) return value == null && otherValue == null;
        if (value instanceof Number && otherValue instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) otherValue).doubleValue();
        }
        return value.equals(otherValue);
    }

    public double add(String key, double value) {
        value = getNumber(key) + value;
        values.put(key, value);
        return value;
    }

    public void remove(String key) {
        values.remove(key);
        Object prev = values.remove(key);
        if (prev != null) post(key, prev, null);
    }

    public void clear() {
        values.clear();
        post(null, null, null);
    }

    public double getNumber(String key) {
        if (key == null || !values.containsKey(key)) return 0;
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }

    public void setNumber(String key, double value) {
        if (key == null) return;
        values.put(key, value);
    }

    // EVENT

    protected void post(String key, Object previous, Object current) {
        Mappet.EVENT_BUS.post(new StateChangedEvent(this, type(), key, previous, current));
    }

    abstract protected TYPES type();

    public enum TYPES {
        SCRIPT, DIALOGUE, FACTION, QUEST
    }

    // NBT

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof Number) {
                tag.setDouble(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }
            else if (entry.getValue() instanceof String) {
                tag.setString(entry.getKey(), (String) entry.getValue());
            }
            else if (entry.getValue() instanceof Boolean) {
                tag.setBoolean(entry.getKey(), (Boolean) entry.getValue());
            }
        }
        return tag;
    }


    public void deserializeNBT(NBTTagCompound tag) {
        values.clear();
        for (String key : tag.getKeySet()) {
            NBTBase base = tag.getTag(key);
            if (base.getId() == Constants.NBT.TAG_STRING) {
                values.put(key, ((NBTTagString) base).getString());
            }
            else if (base instanceof NBTTagDouble) {
                values.put(key, ((NBTTagDouble) base).getDouble());
            }
            else if (base instanceof NBTTagByte) {
                values.put(key, ((NBTTagByte) base).getByte() != 0);
            }
        }
    }
}