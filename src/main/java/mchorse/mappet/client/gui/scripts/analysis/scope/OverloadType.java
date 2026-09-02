package mchorse.mappet.client.gui.scripts.analysis.scope;

import java.util.List;

public class OverloadType implements Type {
    public final List<FunctionType> candidates;

    public OverloadType(List<FunctionType> candidates) {
        this.candidates = candidates;
    }
}
