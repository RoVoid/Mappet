package mchorse.mappet.client.gui.utils.triggers;

import java.util.List;

public class TriggerDoc {
    public String key;
    public String name;
    public String description;
    public boolean cancelable;
    public List<TriggerVariable> variables;

    public TriggerDoc() {
    }

    public TriggerDoc(String key, String name, String description, boolean cancelable, List<TriggerVariable> variables) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.cancelable = cancelable;
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "TriggerDoc{" + "cancelable=" + cancelable + ", key='" + key + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + ", variables=" + variables + '}';
    }
}
