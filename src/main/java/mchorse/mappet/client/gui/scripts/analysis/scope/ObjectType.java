package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.*;

public class ObjectType implements Type {

    public String name;

    public Map<String, Type> fields = new HashMap<>();
    public Map<String, List<FunctionType>> methods = new HashMap<>();

    /** Set only for reflected List/Collection/Set/Iterable/Iterator types: what "for each" over this yields. Null for everything else. */
    public Type elementType;

    public ObjectType() {}

    public ObjectType(String name) {
        this.name = name;
    }
}
