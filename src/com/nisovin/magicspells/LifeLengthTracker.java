package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Map;

import com.nisovin.magicspells.util.TimeUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LifeLengthTracker implements Listener {

	Map<String, Long> lastSpawn = new HashMap<>();
	Map<String, Integer> lastLifeLength = new HashMap<>();
	
	public LifeLengthTracker() {
		Util.forEachPlayerOnline(player -> lastSpawn.put(player.getName(), System.currentTimeMillis()));
		MagicSpells.registerEvents(this);
	}
	
	public int getCurrentLifeLength(Player player) {
		if (lastSpawn.containsKey(player.getName())) {
			long spawn = lastSpawn.get(player.getName());
			return (int)((System.currentTimeMillis() - spawn) / TimeUtil.MILLISECONDS_PER_SECOND);
		}
		return 0;
	}
	
	public int getLastLifeLength(Player player) {
		if (lastLifeLength.containsKey(player.getName())) return lastLifeLength.get(player.getName());
		return 0;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		lastSpawn.put(event.getPlayer().getName(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Long spawn = lastSpawn.remove(event.getPlayer().getName());
		if (spawn != null) lastLifeLength.put(event.getPlayer().getName(), (int)((System.currentTimeMillis() - spawn) / TimeUtil.MILLISECONDS_PER_SECOND));
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Long spawn = lastSpawn.remove(event.getEntity().getName());
		if (spawn != null) lastLifeLength.put(event.getEntity().getName(), (int)((System.currentTimeMillis() - spawn) / TimeUtil.MILLISECONDS_PER_SECOND));
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		lastSpawn.put(event.getPlayer().getName(), System.currentTimeMillis());
	}
	
}
