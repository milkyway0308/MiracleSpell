package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.HashSet;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

public class WindwalkSpell extends BuffSpell {

	private int launchSpeed;
	private float flySpeed;
	int maxY;
	int maxAltitude;
    private boolean cancelOnLand;
	
	HashSet<String> flyers;
	private HashMap<String, Integer> tasks;
	private HeightMonitor heightMonitor = null;
	
	public WindwalkSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.launchSpeed = getConfigInt("launch-speed", 1);
		this.flySpeed = getConfigFloat("fly-speed", 0.1F);
		this.maxY = getConfigInt("max-y", 260);
		this.maxAltitude = getConfigInt("max-altitude", 100);
        this.cancelOnLand = getConfigBoolean("cancel-on-land", true);
		
		this.flyers = new HashSet<>();
		if (this.useCostInterval > 0) this.tasks = new HashMap<>();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (this.cancelOnLand) registerEvents(new SneakListener());
	}

	@Override
	public boolean castBuff(final Player player, float power, String[] args) {
		// Set flying
		if (this.launchSpeed > 0) {
			player.teleport(player.getLocation().add(0, 0.25, 0));
			player.setVelocity(new Vector(0, this.launchSpeed, 0));
		}
		String playerName = player.getName();
		
		this.flyers.add(playerName);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(this.flySpeed);
		// Set cost interval
		if (this.useCostInterval > 0 || this.numUses > 0) {
			int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, () -> addUseAndChargeCost(player), this.useCostInterval, this.useCostInterval);
			
			this.tasks.put(playerName, taskId);
		}
		// Start height monitor
		if (this.heightMonitor == null && (this.maxY > 0 || this.maxAltitude > 0)) {
			this.heightMonitor = new HeightMonitor();
		}
		return true;
	}
    
	public class SneakListener implements Listener {
		
	    @EventHandler(priority=EventPriority.MONITOR)
	    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
	    	Player player = event.getPlayer();
	    	String playerName = player.getName();
	        if (!flyers.contains(playerName)) return;
	        if (player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) return;
	        turnOff(player);
	    }
	    
	}
	
	public class HeightMonitor implements Runnable {
		
		int taskId;
		
		public HeightMonitor() {
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, TimeUtil.TICKS_PER_SECOND, TimeUtil.TICKS_PER_SECOND);
		}
		
		@Override
		public void run() {
			for (String name : flyers) {
				Player p = PlayerNameUtils.getPlayerExact(name);
				if (p != null && p.isValid()) {
					if (maxY > 0) {
						int ydiff = p.getLocation().getBlockY() - maxY;
						if (ydiff > 0) {
							p.setVelocity(p.getVelocity().setY(-ydiff * 1.5));
							continue;
						}
					}
					if (maxAltitude > 0) {
						int ydiff = p.getLocation().getBlockY() - p.getWorld().getHighestBlockYAt(p.getLocation()) - maxAltitude;
						if (ydiff > 0) p.setVelocity(p.getVelocity().setY(-ydiff * 1.5));
					}
				}
			}
		}
		
		public void stop() {
			Bukkit.getScheduler().cancelTask(this.taskId);
		}
		
	}

	@Override
	public void turnOffBuff(final Player player) {
		if (this.flyers.remove(player.getName())) {
			player.setFlying(false);
			if (player.getGameMode() != GameMode.CREATIVE) player.setAllowFlight(false);
			player.setFlySpeed(0.1F);
			player.setFallDistance(0);
		}
		if (this.tasks != null && this.tasks.containsKey(player.getName())) {
			int taskId = this.tasks.remove(player.getName());
			Bukkit.getScheduler().cancelTask(taskId);
		}
		if (this.heightMonitor != null && this.flyers.isEmpty()) {
			this.heightMonitor.stop();
			this.heightMonitor = null;
		}
	}
	
	@Override
	protected void turnOff() {
		HashSet<String> flyers = new HashSet<>(this.flyers);
		for (String name : flyers) {
			Player player = PlayerNameUtils.getPlayerExact(name);
			if (player != null) turnOff(player);
		}
		this.flyers.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return this.flyers.contains(player.getName());
	}

}
