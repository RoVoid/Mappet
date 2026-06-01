package mchorse.mappet.api.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapFactory<T> {
    private final BiMap<String, Class<? extends T>> types = HashBiMap.create();
    private final Map<Class<? extends T>, Integer> colors = new HashMap<>();

    public MapFactory<T> copy() {
        MapFactory<T> newFactory = new MapFactory<>();
        newFactory.types.putAll(types);
        newFactory.colors.putAll(colors);
        return newFactory;
    }

    public int color(T object) {
        Integer color = colors.get(object.getClass());
        return color == null ? 0 : color;
    }

    public int color(Class<? extends T> clazz) {
        Integer color = colors.get(clazz);
        return color == null ? 0 : color;
    }

    public int color(String type) {
        Integer color = colors.get(types.get(type));
        return color == null ? 0 : color;
    }

    public T create(String type) {
        Class<? extends T> clazz = types.get(type);
        if (clazz == null) throw new IllegalStateException("Type \"" + type + "\" is not registered!");
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate type " + type, e);
        }
    }

    public MapFactory<T> register(String type, Class<? extends T> clazz, int color) {
        types.put(type, clazz);
        colors.put(clazz, color);
        return this;
    }

    public String type(Class<? extends T> clazz) {
        return types.inverse().get(clazz);
    }

    public String type(T object) {
        return types.inverse().get(object.getClass());
    }

    public Set<String> types() {
        return types.keySet();
    }

    public MapFactory<T> unregister(String key) {
        Class<? extends T> clazz = types.remove(key);
        colors.remove(clazz);
        return this;
    }
}