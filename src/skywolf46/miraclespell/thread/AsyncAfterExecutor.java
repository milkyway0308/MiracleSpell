package skywolf46.miraclespell.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncAfterExecutor extends Thread {
    private List<Runnable> afterConsumer = new ArrayList<>();
    private final Object LOCK = new Object();
    private AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void run() {
        while (running.get()) {
            try {
                List<Runnable> cons;
                synchronized (LOCK) {
                    cons = new ArrayList<>(afterConsumer);
                    afterConsumer.clear();
                }
                for (Runnable cc : cons) {
                    cc.run();
                }
                Thread.sleep(5);
            } catch (Exception ex) {

            }
        }
    }

    public void add(Runnable r) {
        synchronized (LOCK) {
            afterConsumer.add(r);
        }
    }

    public void stopThread() {
        running.set(false);
        interrupt();
    }
}
