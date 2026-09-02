package mchorse.mappet.client.gui.scripts.completion;

/** A single autocomplete suggestion shown in the overlay. */
public class CompletionSuggestion {
    /** Text drawn in the overlay list, e.g. "getPlayer()". */
    public final String label;
    /** Raw identifier used for matching/sorting, e.g. "getPlayer". */
    public final String name;
    /** True if this suggestion is a method/function (so completing it appends "()"). */
    public final boolean isMethod;

    /** Result/value type shown next to the label, e.g. "EntityPlayer", "number". Null if unknown/not applicable (e.g. plain syntax keywords). */
    public String typeLabel;

    /** jsdoc-style parameter list for methods, e.g. "(target: Player, amount: number)". Null for non-methods or zero-arg methods with nothing worth showing. */
    public String signature;

    public CompletionSuggestion(String name, String label, boolean isMethod) {
        this.name = name;
        this.label = label;
        this.isMethod = isMethod;
    }

    public CompletionSuggestion(String name, String label) {this(name, label, false);}

    public CompletionSuggestion(String name) {this(name, name, false);}

    public CompletionSuggestion withType(String typeLabel) {
        this.typeLabel = typeLabel;
        return this;
    }

    public CompletionSuggestion withSignature(String signature) {
        this.signature = signature;
        return this;
    }
}
