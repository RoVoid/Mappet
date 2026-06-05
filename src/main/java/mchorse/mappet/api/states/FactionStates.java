package mchorse.mappet.api.states;

public class FactionStates extends States {

    public void add(String id, int score, int defaultScore) {
        add(id, has(id) ? score : defaultScore + score);
    }

    public int get(String id) {
        return (int) getNumber(id);
    }

    public void reset(String id) {
        setNumber(id, 0);
    }

    public void resetAll() {
        for (String key : values().keySet()) reset(key);
    }

    public void set(String id, int score) {
        setNumber(id, score);
    }

    @Override
    protected Type type() {
        return Type.FACTION;
    }
}