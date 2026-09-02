package mchorse.mappet.events.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mchorse.mappet.api.utils.IExecutable;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of delayed executions (timer node forks, etc) and ticks
 * them once per server tick.
 * <p>
 * Extracted from {@code EventHandler.onServerTick()}, where it was a
 * single dense block with a comment admitting it "might be a bit
 * confusing". Giving it its own class with a documented {@link #tick()}
 * doesn't change the algorithm, but it isolates the one truly tricky
 * piece of concurrent-modification-avoidance in the old god-class so it
 * can be read, tested and reasoned about on its own.
 */
public class ExecutableScheduler {
    /**
     * Delayed executions
     */
    private final List<IExecutable> executables = new ArrayList<>();

    /**
     * Second executables list to avoid concurrent modification
     * exceptions when adding consequent delayed executions
     */
    private final List<IExecutable> secondList = new ArrayList<>();

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        for (IExecutable executable : executables) {
            ids.add(executable.getId());
        }
        return Lists.newArrayList(Sets.newLinkedHashSet(ids));
    }

    public void addExecutables(List<IExecutable> executionForks) {
        executables.addAll(executionForks);
    }

    public void addExecutable(IExecutable executable) {
        executables.add(executable);
    }

    public int removeExecutables(String id) {
        int size = executables.size();
        executables.removeIf((e) -> e.getId().equals(id));
        return size - executables.size();
    }

    public void reset() {
        executables.clear();
        secondList.clear();
    }

    /**
     * Advance every pending execution fork by one tick, removing those
     * that finished, while still allowing new forks to be scheduled
     * (e.g. by a timer node firing mid-tick) without a
     * {@code ConcurrentModificationException}.
     * <p>
     * This essentially what it does: copy the original forks to a second
     * list and clear the first, ready to receive new forks added while
     * ticking; execute and drop the finished ones from the second list;
     * then merge the survivors and any newly-added forks back into the
     * original list.
     */
    public void tick() {
        if (executables.isEmpty()) return;

        secondList.addAll(executables);
        executables.clear();

        secondList.removeIf(IExecutable::update);

        secondList.addAll(executables);
        executables.clear();
        executables.addAll(secondList);
        secondList.clear();
    }
}
