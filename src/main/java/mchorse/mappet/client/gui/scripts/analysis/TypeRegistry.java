package mchorse.mappet.client.gui.scripts.analysis;

import mchorse.mappet.client.gui.scripts.analysis.scope.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TypeRegistry {

    private final Map<String, Type> namedTypes = new HashMap<>();
    private final Map<Class<?>, Type> classCache = new HashMap<>();


    public void registerClass(String name, Class<?> clazz) {
        namedTypes.put(name, fromClass(clazz));
    }

    public Type resolve(String name) {
        return namedTypes.get(name);
    }

    public Type fromClass(Class<?> clazz) {

        if (classCache.containsKey(clazz)) return classCache.get(clazz);

        // primitives
        if (clazz == int.class || clazz == double.class || clazz == float.class || clazz == long.class || clazz == short.class || clazz == byte.class)
            return PrimitiveType.NUMBER;
        if (clazz == boolean.class) return PrimitiveType.BOOLEAN;
        if (clazz == String.class) return PrimitiveType.STRING;

        // arrays
        if (clazz.isArray()) {
            Type element = fromClass(clazz.getComponentType());
            return new ArrayType(element);
        }

        ObjectType type = new ObjectType();
        classCache.put(clazz, type);

        buildFields(type, clazz);
        buildMethods(type, clazz);

        return type;
    }

    /* ========================================= */

    private void buildFields(ObjectType type, Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            Type fieldType = fromClass(field.getType());
            type.fields.put(field.getName(), fieldType);
        }
    }

    private void buildMethods(ObjectType type, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            FunctionType fn = new FunctionType();
            for (Class<?> param : method.getParameterTypes()) fn.parameterTypes.add(fromClass(param));
            fn.returnType = fromClass(method.getReturnType());
            type.methods.put(method.getName(), fn);
        }
    }
}

