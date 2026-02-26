package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.*;

public class ObjectType implements Type {

    public Map<String, Type> fields = new HashMap<>();
    public Map<String, FunctionType> methods = new HashMap<>();
}

