package mchorse.mappet.api.states;

public class DialogueStates extends States {
    public int getReadTimes(String id, String marker) {
        return (int) getNumber(key(id, marker));
    }

    private String key(String id, String marker) {
        return marker == null || marker.isEmpty() ? id : id + ":" + marker;
    }

    public void read(String id, String marker) {
        add(key(id, marker), 1);
    }

    @Override
    protected TYPES type() {
        return TYPES.DIALOGUE;
    }

    public boolean wasRead(String id, String marker) {
        return getReadTimes(id, marker) > 0;
    }
}