package mchorse.mappet.api.states;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ScriptStates extends States {
    private final Map<String, ValueType> keyTypes = new HashMap<>();

    public enum ValueType {
        NUMBER, STRING, BOOLEAN
    }

    public void keyProtect(String key, ValueType type) {
        if (key == null || type == null) return;
        keyTypes.put(key, type);
    }

    public void keyUnprotect(String key, ValueType type) {
        if (key == null || type == null) return;
        keyTypes.remove(key);
    }

    public boolean cannotAssign(String key, Object value) {
        if (key == null) return true;
        if (value == null) return true;

        ValueType type = keyTypes.get(key);
        switch (type) {
            case NUMBER:
                return !(value instanceof Number);
            case STRING:
                return !(value instanceof String);
            case BOOLEAN:
                return !(value instanceof Boolean);
            default:
                return false;
        }
    }

    @Override
    public double add(String key, double value) {
        if (cannotAssign(key, value)) return 0;

        Object prev = values.get(key);
        if (prev != null && !(prev instanceof Number)) return 0;

        double result = (prev == null ? 0 : ((Number) prev).doubleValue()) + value;
        post(key, prev, values.put(key, result));
        return result;
    }

    public String add(String key, String value) {
        if (cannotAssign(key, value)) return "";

        Object prev = values.get(key);
        if (prev != null && !(prev instanceof String)) return "";

        String result = (prev == null ? "" : (String) prev) + value;
        post(key, prev, values.put(key, result));
        return result;
    }

    public boolean toggle(String key) {
        if (cannotAssign(key, true)) return false;

        Object prev = values.get(key);
        if (prev != null && !(prev instanceof Boolean)) return false;

        boolean result = prev == null || !(Boolean) prev;
        post(key, prev, values.put(key, result));
        return result;
    }

    @Override
    public void remove(String key) {
        super.remove(key);
        keyTypes.remove(key);
    }

    public void reset(String key) {
        Object prev;
        if (!keyTypes.containsKey(key)) {
            prev = values.remove(key);
            if (prev != null) post(key, prev, null);
        }
        prev = values.get(key);
        switch (keyTypes.get(key)) {
            case NUMBER:
                values.put(key, 0);
                break;
            case STRING:
                values.put(key, "");
                break;
            case BOOLEAN:
                values.put(key, false);
                break;
        }
        if (prev != values.get(key)) post(key, prev, values.get(key));
    }

    private static final String WILDCARD = "*";

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
        if (cannotAssign(key, value)) return;
        post(key, values.put(key, value), value);
    }

    @Override
    public void setNumber(String key, double value) {
        if (cannotAssign(key, value)) return;
        if (Double.isNaN(value)) return;
        post(key, values.put(key, value), value);
    }

    public void setString(String key, String value) {
        if (cannotAssign(key, value)) return;
        if (value == null) return;
        post(key, values.put(key, value), value);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = values.get(key);
        return val instanceof Boolean ? (Boolean) val : defaultValue;
    }

    @Override
    public double getNumber(String key) {
        return getNumber(key, 0);
    }

    public double getNumber(String key, double defaultValue) {
        Object val = values.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : defaultValue;
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        Object val = values.get(key);
        return val instanceof String ? (String) val : defaultValue;
    }

    public boolean isBoolean(String key) {
        return values.get(key) instanceof Boolean || keyTypes.get(key) == ValueType.BOOLEAN;
    }

    public boolean isNumber(String key) {
        return values.get(key) instanceof Number || keyTypes.get(key) == ValueType.NUMBER;
    }

    public boolean isString(String key) {
        return values.get(key) instanceof String || keyTypes.get(key) == ValueType.STRING;
    }

    @Override
    protected TYPES type() {
        return TYPES.SCRIPT;
    }
}