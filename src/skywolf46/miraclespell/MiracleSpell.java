package skywolf46.miraclespell;

import com.nisovin.magicspells.Spellbook;
import org.bukkit.entity.Player;
import skywolf46.miraclespell.data.FutureSpellbook;
import skywolf46.miraclespell.thread.AsyncAfterExecutor;
import skywolf46.miraclespell.util.ObservableFuture;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MiracleSpell {
    private static HashMap<Player, ObservableFuture<Spellbook>> beautifulFuture = new HashMap<>();
    private static final AsyncAfterExecutor after = new AsyncAfterExecutor();
    private static final AsyncAfterExecutor particle = new AsyncAfterExecutor();

    public static void init() {
        after.start();
        particle.start();

    }


    public static ObservableFuture<Spellbook> getSpellBook(Player p) {
        return beautifulFuture.computeIfAbsent(p, str -> {
            // If null, no future to you
            // - What?
            return FutureSpellbook.futureOf(p);
        });
    }

    public static void destroySpellbook(Player p) {
        Future<Spellbook> spBook = beautifulFuture.remove(p);
        spBook.cancel(true);
        if (spBook.isDone()) {
            try {
                spBook.get().destroy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    public static void stop() {
        after.stopThread();
    }

    public static AsyncAfterExecutor getAfterExecutor() {
        return after;
    }


    public static AsyncAfterExecutor getParticleExecutor() {
        return particle;
    }
}
