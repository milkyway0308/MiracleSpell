package skywolf46.miraclespell.data;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import org.bukkit.entity.Player;
import skywolf46.miraclespell.util.ObservableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureSpellbook implements ObservableFuture<Spellbook> {
    private Spellbook sBook;
    private Player p;

    private FutureSpellbook(Player p) {
        this.p = p;
    }

    public static ObservableFuture<Spellbook> futureOf(Player p) {
        return new FutureSpellbook(p);
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Spellbook get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Spellbook get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Spellbook observe() {
        return sBook;
    }

    class Spellbookprovider implements Runnable {
        @Override
        public void run() {
            sBook = new Spellbook(p, MagicSpells.getInstance());
            // Relase player
            p = null;
        }
    }
}
