package mchorse.mappet.api.states;

public class QuestStates extends States {
    public void complete(String id) {
        add(id, 1);
    }

    public int getCompletedTimes(String id) {
        return (int) getNumber(id);
    }

    @Override
    protected Type type() {
        return Type.QUEST;
    }

    public boolean wasCompleted(String id) {
        return getCompletedTimes(id) > 0;
    }
}