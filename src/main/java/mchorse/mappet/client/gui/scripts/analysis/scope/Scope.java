package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.*;

public class Scope {

    public final Scope parent;
    private final Map<String, Symbol> symbols = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.name, symbol);
    }

    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        return parent != null ? parent.resolve(name) : null;
    }
}

