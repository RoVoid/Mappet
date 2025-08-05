package mchorse.mappet.client.gui.utils.triggers;

public class TriggerVariable {
    public String type;
    public String name;
    public String description;

    public TriggerVariable() {
    }

    public TriggerVariable(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "TriggerVariable{" + "description='" + description + '\'' + ", type='" + type + '\'' + ", name='" + name + '\'' + '}';
    }
}
