package mchorse.mappet.client.gui.scripts.analysis.scope;

public class ArrayType implements Type {
    public Type elementType;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
    }
}
