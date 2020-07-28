package skywolf46.miraclespell.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class ObservableFuture<V> implements Future<V> {
    private List<Runnable> listeners = new ArrayList<>();
    private final Object LOCK = new Object();

    public abstract V observe();

    protected void done() {
        listeners.forEach(le -> le.run());
        listeners.clear();
        listeners = null;
    }

    public final void reserve(Runnable action) {
        synchronized (getLock()) {
            if (!isDone()) {
                listeners.add(action);
                return;
            }
        }
        action.run();
    }

    public final void reserve(Consumer<ObservableFuture<V>> action) {
        reserve(() -> action.accept(ObservableFuture.this));
    }

    public final void syncReserve(Consumer<ObservableFuture<V>> action) {
        reserve(BukkitForceRunnable.create(() -> action.accept(this)));
    }

    protected final Object getLock() {
        return LOCK;
    }

}
