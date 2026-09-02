package mchorse.mappet.api.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.events.StateChangedEvent;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.regex.Pattern;

public abstract class States implements INBTSerializable<NBTTagCompound> {
    protected final Map<String, Object> values = new HashMap<>();
    public Object owner = null;

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

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public boolean equals(String key, Object otherValue) {
        Object value = values.get(key);
        if (value == null || otherValue == null) return value == null && otherValue == null;
        if (value instanceof Number && otherValue instanceof Number) return ((Number) value).doubleValue() == ((Number) otherValue).doubleValue();
        return value.equals(otherValue);
    }

    public double add(String key, double value) {
        double previous = getNumber(key);
        double result = previous + value;

        values.put(key, result);
        post(key, previous, result);

        return result;
    }

    public void remove(String key) {
        Object prev = values.remove(key);
        if (prev != null) post(key, prev, null);
    }

    public void removeMasked(String mask) {
        if (mask == null || values.isEmpty()) return;

        mask = mask.trim();
        if (mask.isEmpty()) return;
        if (mask.charAt(0) == '*') {
            clear();
            for (Map.Entry<String, Object> entry : values.entrySet()) post(entry.getKey(), entry.getValue(), null);
        }
        else if (mask.indexOf('*') >= 0) {
            Pattern pattern = Pattern.compile("^" + Pattern.quote(mask).replace("\\*", ".*") + "$");
            List<String> toRemove = new ArrayList<>();
            for (String key : values.keySet()) if (pattern.matcher(key).matches()) toRemove.add(key);
            for (String key : toRemove) remove(key);
        }
        else remove(mask);
    }

    public void clear() {
        values.clear();
    }

    public double getNumber(String key) {
        if (key == null || !values.containsKey(key)) return 0;
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }

    public void setNumber(String key, double value) {
        if (key == null || Double.isNaN(value)) return;
        Object prev = values.get(key);
        values.put(key, value);
        post(key, prev, value);
    }

    // EVENT

    protected void post(String key, Object previous, Object current) {
        Mappet.logger.debug(key, previous, current, Objects.equals(previous, current));
        if (key != null && !Objects.equals(previous, current)) Mappet.EVENT_BUS.post(new StateChangedEvent(this, type(), key, previous, current));
    }

    abstract protected Type type();

    public enum Type {
        SCRIPT, DIALOGUE, FACTION, QUEST;

        String id() {return name().toLowerCase();}
    }

    // NBT

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Number) tag.setDouble(entry.getKey(), ((Number) val).doubleValue());
            else if (val instanceof String) tag.setString(entry.getKey(), (String) val);
            else if (val instanceof Boolean) tag.setBoolean(entry.getKey(), (Boolean) val);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        values.clear();
        for (String key : tag.getKeySet()) {
            NBTBase base = tag.getTag(key);
            if (base.getId() == Constants.NBT.TAG_STRING) values.put(key, ((NBTTagString) base).getString());
            else if (base instanceof NBTTagDouble) values.put(key, ((NBTTagDouble) base).getDouble());
            else if (base instanceof NBTTagByte) values.put(key, ((NBTTagByte) base).getByte() != 0);
        }
    }
}