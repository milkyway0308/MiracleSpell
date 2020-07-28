package skywolf46.miraclespell.util;

import com.nisovin.magicspells.MagicSpells;
import org.bukkit.Bukkit;

public class BukkitForceRunnable implements Runnable {
    private Runnable r;

    private BukkitForceRunnable(Runnable r) {
        this.r = r;
    }

    @Override
    public void run() {
        Bukkit.getScheduler().runTask(MagicSpells.getInstance(), r);
    }

    public static BukkitForceRunnable create(Runnable r) {
        return new BukkitForceRunnable(r);
    }
}
