package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.*;

public class Scope {

    public final Scope parent;

    public int start;
    public int end = Integer.MAX_VALUE;

    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<Scope> children = new ArrayList<>();

    public Scope(Scope parent) {
        this.parent = parent;
        if (parent != null) parent.children.add(this);
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.name, symbol);
    }

    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        return parent != null ? parent.resolve(name) : null;
    }

    public Collection<Symbol> getOwnSymbols() {
        return symbols.values();
    }

    public List<Scope> getChildren() {
        return children;
    }

    public Scope findScopeAt(int offset) {
        if (!contains(offset)) return null;

        for (Scope child : children) {
            Scope found = child.findScopeAt(offset);
            if (found != null) return found;
        }

        return this;
    }

    private boolean contains(int offset) {
        return offset >= start && offset <= end;
    }
}
