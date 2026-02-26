package mchorse.mappet.client.gui.scripts.analysis.scope;

public class PrimitiveType implements Type {
    public static final PrimitiveType NUMBER = new PrimitiveType("number");
    public static final PrimitiveType STRING = new PrimitiveType("string");
    public static final PrimitiveType BOOLEAN = new PrimitiveType("boolean");
    public static final PrimitiveType UNKNOWN = new PrimitiveType("unknown");

    public final String name;

    public PrimitiveType(String name) {
        this.name = name;
    }
}

