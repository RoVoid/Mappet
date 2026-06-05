package mchorse.mappet.api.states;

import java.util.regex.Pattern;

public class ScriptStates extends States {
    @Override
    public double add(String key, double value) {
        Object prev = values.get(key);
        if (prev != null && !(prev instanceof Number)) return 0;

        double result = (prev == null ? 0 : ((Number) prev).doubleValue()) + value;
        values.put(key, result);
        post(key, prev, result);
        return result;
    }

    public String add(String key, String value) {
        Object prev = values.get(key);
        if (prev != null && !(prev instanceof String)) return "";

        String result = (prev == null ? "" : (String) prev) + value;
        values.put(key, result);
        post(key, prev, result);
        return result;
    }

    public boolean toggle(String key) {
        Object prev = values.get(key);
        if (prev != null && !(prev instanceof Boolean)) return false;

        boolean result = prev == null || !(Boolean) prev;
        values.put(key, result);
        post(key, prev, result);
        return result;
    }

    public void reset(String key) {
        remove(key);
    }

    public void resetMasked(String mask) {
        if (mask == null || values.isEmpty()) return;

        mask = mask.trim();
        if (mask.isEmpty()) return;

        if (mask.charAt(0) == '*') clear();
        else if (mask.indexOf('*') >= 0) {
            Pattern pattern = Pattern.compile("^" + Pattern.quote(mask).replace("\\*", ".*") + "$");
            if (values.keySet().removeIf(key -> pattern.matcher(key).matches())) post(null, null, null);
        }
        else reset(mask);
    }

    public void setBoolean(String key, boolean value) {
        if (key == null) return;
        Object prev = values.get(key);
        values.put(key, value);
        post(key, prev, value);
    }

    public void setString(String key, String value) {
        if (key == null || value == null) return;
        Object prev = values.get(key);
        values.put(key, value);
        post(key, prev, value);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (key == null || !values.containsKey(key)) return defaultValue;
        Object val = values.get(key);
        return val instanceof Boolean ? (Boolean) val : defaultValue;
    }

    public double getNumber(String key, double defaultValue) {
        if (key == null || !values.containsKey(key)) return defaultValue;
        Object val = values.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : defaultValue;
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        if (key == null || !values.containsKey(key)) return defaultValue;
        Object val = values.get(key);
        return val instanceof String ? (String) val : defaultValue;
    }

    public boolean isBoolean(String key) {
        return values.get(key) instanceof Boolean;
    }

    public boolean isNumber(String key) {
        return values.get(key) instanceof Number;
    }

    public boolean isString(String key) {
        return values.get(key) instanceof String;
    }

    @Override
    protected Type type() {
        return Type.SCRIPT;
    }
}