package mchorse.mappet.api.states;

import jdk.nashorn.internal.objects.NativeJSON;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.user.mappet.IMappetStates;
import mchorse.mappet.events.StateChangedEvent;
import mchorse.mappet.utils.NBTToJsonLike;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class States implements IMappetStates, INBTSerializable<NBTTagCompound> {
    public static final String QUEST_PREFIX = "quests.";
    public static final String DIALOGUE_PREFIX = "dialogue.";
    public static final String FACTIONS_PREFIX = "factions.";

    private final Map<String, Object> values = new HashMap<>();

    private File file;

    public States() {
    }

    public States(File file) {
        this.file = file;
    }

    public States(States other) {
        copy(other);
    }

    protected void post(String id, Object previous, Object current) {
        Mappet.EVENT_BUS.post(new StateChangedEvent(this, id, previous, current));
    }

    public void copy(States states) {
        copy(states, false);
    }

    public void copy(States states, boolean withoutPost) {
        if (states == null) return;
        values.clear();
        values.putAll(states.values);
        if (!withoutPost) post(null, null, null);
    }

    /* Quests */
    public void completeQuest(String id) {
        add(QUEST_PREFIX + id, 1);
    }

    public int getQuestCompletedTimes(String id) {
        return (int) getNumber(QUEST_PREFIX + id);
    }

    public boolean wasQuestCompleted(String id) {
        return getQuestCompletedTimes(id) > 0;
    }

    /* Factions */
    public void addFactionScore(String id, int score, int defaultScore) {
        if (hasFaction(id)) add(FACTIONS_PREFIX + id, score);
        else setNumber(FACTIONS_PREFIX + id, defaultScore + score);
    }

    public void setFactionScore(String id, int score) {
        setNumber(FACTIONS_PREFIX + id, score);
    }

    public int getFactionScore(String id) {
        return (int) getNumber(FACTIONS_PREFIX + id);
    }

    public void clearFactionScore(String id) {
        reset(FACTIONS_PREFIX + id);
    }

    public void clearAllFactionScores() {
        values.keySet().removeIf((key) -> key.startsWith(FACTIONS_PREFIX));
    }

    public boolean hasFaction(String id) {
        return values.containsKey(FACTIONS_PREFIX + id);
    }

    public Set<String> getFactionNames() {
        Set<String> factionNames = new HashSet<>();
        for (String key : values.keySet()) {
            if (key.startsWith(FACTIONS_PREFIX)) factionNames.add(key.replace(FACTIONS_PREFIX, ""));
        }
        return factionNames;
    }

    /* Dialogues */
    public void readDialogue(String id, String marker) {
        add(getDialogueId(id, marker), 1);
    }

    public boolean hasReadDialogue(String id, String marker) {
        return getReadDialogueTimes(id, marker) > 0;
    }

    public int getReadDialogueTimes(String id, String marker) {
        return (int) getNumber(getDialogueId(id, marker));
    }

    private String getDialogueId(String id, String marker) {
        id = DIALOGUE_PREFIX + id;
        if (marker != null && !marker.isEmpty()) {
            id += ":" + marker;
        }
        return id;
    }

    public boolean areValuesEqual(String key, Object otherValue) {
        Object value = values.get(key);
        if (value == null && otherValue == null) return true;
        if (value == null || otherValue == null) return false;
        if (value instanceof Number && otherValue instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) otherValue).doubleValue();
        }
        return value.equals(otherValue);
    }

    /* Load/save */
    public void load() {
        if (!file.exists()) return;
        try {
            deserializeNBT(NBTToJsonLike.read(file));
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    public boolean save() {
        try {
            NBTToJsonLike.write(file, serializeNBT());
            return true;
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
        return false;
    }

    public Map<String, Object> values() {
        return values;
    }

    /* Scripting */

    @Override
    public double add(String id, double value) {
        Object prev = values.get(id);
        if (prev != null && !(prev instanceof Number)) return 0;

        double result = (prev == null ? 0 : ((Number) prev).doubleValue()) + value;
        post(id, prev, values.put(id, result));
        return result;
    }

    @Override
    public String add(String id, String value) {
        Object prev = values.get(id);
        if (prev != null && !(prev instanceof String)) return "";

        String result = (prev == null ? "" : (String) prev) + value;
        post(id, prev, values.put(id, result));
        return result;
    }

    @Override
    public boolean toggle(String id) {
        Object prev = values.get(id);
        if (prev != null && !(prev instanceof Boolean)) return false;

        boolean result = prev == null || !(Boolean) prev;
        post(id, prev, values.put(id, result));
        return result;
    }

    @Override
    public void setNumber(String id, double value) {
        if (Double.isNaN(value)) return;
        post(id, values.put(id, value), value);
    }

    @Override
    public void setBoolean(String id, boolean value) {
        post(id, values.put(id, value), value);
    }

    @Override
    public void setString(String id, String value) {
        if (value == null) return;
        post(id, values.put(id, value), value);
    }

    @Override
    public void setJson(String id, Object value) {
        if (value == null) return;
        setString(id, (String) NativeJSON.stringify(null, value, null, null));
    }


    @Override
    public double getNumber(String id) {
        return getNumber(id, 0);
    }

    @Override
    public double getNumber(String id, double defaultValue) {
        Object val = values.get(id);
        return val instanceof Number ? ((Number) val).doubleValue() : 0;
    }

    @Override
    public boolean getBoolean(String id) {
        return getBoolean(id, false);
    }

    @Override
    public boolean getBoolean(String id, boolean defaultValue) {
        return Boolean.TRUE.equals(values.get(id)) && defaultValue;
    }

    @Override
    public String getString(String id) {
        return getString(id, "");
    }

    @Override
    public String getString(String id, String defaultValue) {
        Object val = values.get(id);
        return val instanceof String ? (String) val : defaultValue;
    }

    @Override
    public Object getJson(String id) {
        return getJson(id, "{}");
    }

    @Override
    public Object getJson(String id, String defaultValue) {
        String raw = getString(id, defaultValue);
        return NativeJSON.parse(null, raw, null);
    }

    @Override
    public boolean isNumber(String id) {
        return values.get(id) instanceof Number;
    }

    @Override
    public boolean isBoolean(String id) {
        return values.get(id) instanceof Boolean;
    }

    @Override
    public boolean isString(String id) {
        return values.get(id) instanceof String;
    }

    //@Override
    public boolean isJson(String id) {
        return values.get(id) instanceof String;
    }

    @Override
    public boolean reset(String id) {
        Object prev = values.remove(id);
        post(id, prev, null);
        return prev != null;
    }

    @Override
    public boolean resetMasked(String id) {
        if (id.trim().equals("*")) {
            if (values.isEmpty()) return false;
            clear();
            return true;
        }

        if (id.contains("*")) {
            Pattern pattern = Pattern.compile("^" + id.replace("*", ".*") + "$");
            int before = values.size();
            values.keySet().removeIf(key -> pattern.matcher(key).matches());
            boolean changed = values.size() != before;
            if (changed) post(null, null, null);
            return changed;
        }

        return reset(id);
    }

    @Override
    public void clear() {
        values.clear();
        post(null, null, null);
    }

    @Override
    public boolean has(String id) {
        return values.containsKey(id);
    }

    @Override
    public Set<String> keys() {
        return new HashSet<>(values.keySet());
    }


    /* NBT */
    @Override
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

    @Override
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