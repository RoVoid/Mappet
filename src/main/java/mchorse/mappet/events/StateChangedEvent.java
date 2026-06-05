package mchorse.mappet.events;

import mchorse.mappet.api.states.States;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StateChangedEvent extends Event {
    public final States states;
    public final States.Type type;
    public final String key;
    public final Object previous;
    public final Object current;

    public StateChangedEvent(States states, States.Type type, String key, Object previous, Object current) {
        this.states = states;
        this.type = type;
        this.key = key;
        this.previous = previous;
        this.current = current;
    }
}