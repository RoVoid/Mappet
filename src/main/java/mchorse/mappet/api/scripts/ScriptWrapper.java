package mchorse.mappet.api.scripts;


public abstract class ScriptWrapper<T> {
    private final T object;

    protected ScriptWrapper(T object) {
        this.object = object;
    }

    /**
     * @return the underlying object
     */
    public T base() {
        return object;
    }

    public T asMinecraft() {
        return object;
    }
}
