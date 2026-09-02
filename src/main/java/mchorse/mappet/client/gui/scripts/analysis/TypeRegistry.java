package mchorse.mappet.client.gui.scripts.analysis;

import mchorse.mappet.client.gui.scripts.analysis.scope.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeRegistry {

    private final Map<String, Type> namedTypes = new HashMap<>();
    private final Map<Class<?>, Type> classCache = new HashMap<>();
    private final Map<String, Type> classNameCache = new HashMap<>();

    private static final String MAPPET_SHORTHAND = "mappet.";
    private static final String MAPPET_PACKAGE = "mchorse.mappet.api.scripts.code.";

    public void registerClass(String name, Class<?> clazz) {
        namedTypes.put(name, fromClass(clazz));
    }

    public Type resolve(String name) {
        return namedTypes.get(name);
    }

    /**
     * Resolves a JSDoc {@code @param}/{@code @return} type name, in order:
     * primitives ("number"/"string"/"boolean"), array suffix ("Foo[]"), names
     * registered via {@link #registerClass}, the "mappet.X" shorthand for
     * "mchorse.mappet.api.scripts.code.X", and finally any fully-qualified
     * class name reachable on the classpath. Returns null if none of that resolves.
     */
    public Type resolveTypeName(String name) {
        if (name == null) return null;

        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;

        if (trimmed.endsWith("[]")) {
            Type element = resolveTypeName(trimmed.substring(0, trimmed.length() - 2).trim());
            return element != null ? new ArrayType(element) : null;
        }

        switch (trimmed.toLowerCase()) {
            case "number": return PrimitiveType.NUMBER;
            case "string": return PrimitiveType.STRING;
            case "boolean": return PrimitiveType.BOOLEAN;
        }

        int lt = trimmed.indexOf('<');
        if (lt > 0 && trimmed.endsWith(">")) {
            String outer = trimmed.substring(0, lt);
            Type inner = resolveTypeName(trimmed.substring(lt + 1, trimmed.length() - 1));

            switch (outer.toLowerCase()) {
                case "list": return listType(java.util.List.class, inner);
                case "collection": return listType(java.util.Collection.class, inner);
                case "set": return listType(java.util.Set.class, inner);
                case "iterable": return listType(Iterable.class, inner);
                case "array":
                    return new ArrayType(inner != null ? inner : PrimitiveType.UNKNOWN);
            }

            // Not a recognized collection shape — fall back to whatever the outer name resolves to
            // (ignoring the generic part, since we have nowhere else to put it).
            Type outerType = resolveTypeName(outer);
            if (outerType != null) return outerType;
        }

        Type named = namedTypes.get(trimmed);
        if (named != null) return named;

        String className = trimmed.startsWith(MAPPET_SHORTHAND)
                ? MAPPET_PACKAGE + trimmed.substring(MAPPET_SHORTHAND.length())
                : trimmed;

        if (className.indexOf('.') < 0) return null; // bare unknown identifier, not a fully-qualified class

        return resolveClassName(className);
    }

    private Type listType(Class<?> raw, Type element) {
        Type base = fromClass(raw);
        if (!(base instanceof ObjectType)) return base;

        ObjectType shared = (ObjectType) base;
        ObjectType wrapped = new ObjectType(shared.name);
        wrapped.fields = shared.fields;
        wrapped.methods = shared.methods;
        wrapped.elementType = element != null ? element : PrimitiveType.UNKNOWN;
        return wrapped;
    }

    private Type resolveClassName(String className) {
        if (classNameCache.containsKey(className)) return classNameCache.get(className);

        Type type;
        try {
            type = fromClass(Class.forName(className, false, getClass().getClassLoader()));
        }
        catch (Throwable e) {
            type = null;
        }

        classNameCache.put(className, type);
        return type;
    }

    public Map<String, Type> getNamedTypes() {
        return java.util.Collections.unmodifiableMap(namedTypes);
    }

    public Type fromClass(Class<?> clazz) {
        if (classCache.containsKey(clazz)) return classCache.get(clazz);

        if (clazz == int.class || clazz == double.class || clazz == float.class || clazz == long.class || clazz == short.class || clazz == byte.class)
            return PrimitiveType.NUMBER;
        if (clazz == boolean.class) return PrimitiveType.BOOLEAN;
        if (clazz == String.class) return PrimitiveType.STRING;

        if (clazz.isArray()) return new ArrayType(fromClass(clazz.getComponentType()));

        ObjectType type = new ObjectType(clazz.getSimpleName());
        classCache.put(clazz, type);

        buildFields(type, clazz);
        buildMethods(type, clazz);

        return type;
    }

    /** Interfaces treated as "a collection of T" when resolving List&lt;T&gt;-style generics — iterating one of these (directly, or via "for each") yields T rather than the raw/erased type. */
    private static final Class<?>[] LIST_LIKE = {List.class, java.util.Collection.class, java.util.Set.class, Iterable.class, java.util.Iterator.class};

    /**
     * Like {@link #fromClass}, but resolves generic type information when available (field/method
     * declarations, not the runtime object) — e.g. a method returning {@code List<Player>} resolves
     * to {@code ArrayType(Player)} instead of an opaque {@code List}, so "for each" over it and "[i]"
     * indexing into it both know the element type.
     */
    public Type fromType(java.lang.reflect.Type reflectType) {
        if (reflectType instanceof Class<?>) return fromClass((Class<?>) reflectType);

        if (reflectType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) reflectType;
            java.lang.reflect.Type raw = pt.getRawType();

            if (raw instanceof Class<?> && isListLike((Class<?>) raw)) {
                java.lang.reflect.Type[] args = pt.getActualTypeArguments();
                Type element = args.length > 0 ? fromType(args[0]) : PrimitiveType.UNKNOWN;
                Type base = fromClass((Class<?>) raw);

                if (base instanceof ObjectType) {
                    // Share the reflected fields/methods (built once and cached per raw Class) but
                    // don't mutate the cached instance: List<Player> and List<Item> need different
                    // elementType while keeping the same real List methods (add/get/size/remove/...).
                    ObjectType shared = (ObjectType) base;
                    ObjectType wrapped = new ObjectType(shared.name);
                    wrapped.fields = shared.fields;
                    wrapped.methods = shared.methods;
                    wrapped.elementType = element;
                    return wrapped;
                }

                return base;
            }

            return raw instanceof Class<?> ? fromClass((Class<?>) raw) : PrimitiveType.UNKNOWN;
        }

        if (reflectType instanceof java.lang.reflect.GenericArrayType) {
            java.lang.reflect.Type component = ((java.lang.reflect.GenericArrayType) reflectType).getGenericComponentType();
            return new ArrayType(fromType(component));
        }

        // An unresolved type parameter (e.g. "T" itself) or wildcard ("? extends Foo") — fall back
        // to its first bound, which is the closest useful type we can offer without call-site reification.
        if (reflectType instanceof java.lang.reflect.TypeVariable<?>) {
            java.lang.reflect.Type[] bounds = ((java.lang.reflect.TypeVariable<?>) reflectType).getBounds();
            return bounds.length > 0 ? fromType(bounds[0]) : PrimitiveType.UNKNOWN;
        }

        if (reflectType instanceof java.lang.reflect.WildcardType) {
            java.lang.reflect.Type[] upper = ((java.lang.reflect.WildcardType) reflectType).getUpperBounds();
            return upper.length > 0 ? fromType(upper[0]) : PrimitiveType.UNKNOWN;
        }

        return PrimitiveType.UNKNOWN;
    }

    private boolean isListLike(Class<?> raw) {
        for (Class<?> c : LIST_LIKE) if (c.isAssignableFrom(raw)) return true;
        return false;
    }

    /* ========================================= */

    private void buildFields(ObjectType type, Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            type.fields.put(field.getName(), fromType(field.getGenericType()));
        }
    }

    private void buildMethods(ObjectType type, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            FunctionType fn = new FunctionType();

            for (Parameter param : method.getParameters()) {
                fn.parameterTypes.add(fromType(param.getParameterizedType()));
                // param.getName() only returns real source names when the class was compiled
                // with -parameters; otherwise it falls back to "argN".
                fn.parameterNames.add(param.getName());
            }

            fn.returnType = fromType(method.getGenericReturnType());

            List<FunctionType> overloads = type.methods.computeIfAbsent(method.getName(), k -> new ArrayList<>());
            if (!containsSameSignature(overloads, fn)) overloads.add(fn);
        }
    }

    /** getMethods() can report the same signature twice (bridge/synthetic methods from generics or interface inheritance); skip exact duplicates so they don't show up as fake overloads. */
    private boolean containsSameSignature(List<FunctionType> overloads, FunctionType candidate) {
        for (FunctionType existing : overloads) {
            if (existing.parameterTypes.size() != candidate.parameterTypes.size()) continue;
            if (describeParameters(existing).equals(describeParameters(candidate))) return true;
        }
        return false;
    }

    /** Picks the overload whose parameter count is closest to {@code argCount}, preferring an exact match. {@code argCount < 0} means "no call info yet" and just returns the first candidate. */
    public static FunctionType pickOverload(List<FunctionType> overloads, int argCount) {
        if (overloads == null || overloads.isEmpty()) return null;
        if (argCount < 0) return overloads.get(0);

        FunctionType best = null;
        int bestDiff = Integer.MAX_VALUE;

        for (FunctionType fn : overloads) {
            int diff = Math.abs(fn.parameterTypes.size() - argCount);
            if (diff == 0) return fn;
            if (diff < bestDiff) {
                bestDiff = diff;
                best = fn;
            }
        }

        return best;
    }

    /** Picks the overload with the fewest parameters that still covers {@code activeParameter} (0-based), for signature help while typing arguments. Falls back to the highest-arity overload if none covers it. */
    public static FunctionType pickOverloadForParameter(List<FunctionType> overloads, int activeParameter) {
        if (overloads == null || overloads.isEmpty()) return null;

        FunctionType best = null;
        FunctionType widest = overloads.get(0);

        for (FunctionType fn : overloads) {
            if (fn.parameterTypes.size() > widest.parameterTypes.size()) widest = fn;
            if (fn.parameterTypes.size() <= activeParameter) continue;
            if (best == null || fn.parameterTypes.size() < best.parameterTypes.size()) best = fn;
        }

        return best != null ? best : widest;
    }

    public static String describe(Type type) {
        if (type == null) return "unknown";
        if (type instanceof PrimitiveType) return ((PrimitiveType) type).name;
        if (type instanceof ArrayType) return describe(((ArrayType) type).elementType) + "[]";
        if (type instanceof FunctionType) return describe(((FunctionType) type).returnType);
        if (type instanceof OverloadType) {
            FunctionType fn = pickOverload(((OverloadType) type).candidates, -1);
            return fn != null ? describe(fn.returnType) : "unknown";
        }
        if (type instanceof ObjectType) {
            ObjectType obj = (ObjectType) type;
            String name = obj.name != null ? obj.name : "object";
            return obj.elementType != null ? name + "<" + describe(obj.elementType) + ">" : name;
        }
        return "unknown";
    }

    /** jsdoc-style parameter list for a method's signature, e.g. "(target: Player, amount: number)". */
    public static String describeParameters(FunctionType fn) {
        StringBuilder sb = new StringBuilder("(");

        for (int i = 0; i < fn.parameterTypes.size(); i++) {
            if (i > 0) sb.append(", ");

            String name = i < fn.parameterNames.size() ? fn.parameterNames.get(i) : "arg" + i;
            sb.append(name).append(": ").append(describe(fn.parameterTypes.get(i)));
        }

        return sb.append(")").toString();
    }

    /** Signature preview for a completion suggestion: the first (usually simplest) overload's parameter list, with a "+N overloads" suffix when there's more than one. */
    public static String describeOverloads(List<FunctionType> overloads) {
        String primary = describeParameters(overloads.get(0));
        if (overloads.size() == 1) return primary;

        int extra = overloads.size() - 1;
        return primary + " (+" + extra + (extra == 1 ? " overload)" : " overloads)");
    }
}
