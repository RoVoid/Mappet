package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.*;

public class FunctionType implements Type {
    public List<Type> parameterTypes = new ArrayList<>();
    public Type returnType = PrimitiveType.UNKNOWN;
}


