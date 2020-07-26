package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.nisovin.magicspells.MagicSpells;

public class BossBarManager_V1_8 implements BossBarManager, Listener {

	Map<String, String> bossBarTitles = new HashMap<>();
	Map<String, Double> bossBarValues = new HashMap<>();
	
	public BossBarManager_V1_8() {
		MagicSpells.registerEvents(this);
		
		MagicSpells.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				for (String name : bossBarTitles.keySet()) {
					Player player = Bukkit.getPlayerExact(name);
					if (player == null) continue;
					updateBar(player, null, 0);
				}
			}
			
		}, 8, 8);
	}
	
	@Override
	public void setPlayerBar(Player player, String title, double percent) {
		boolean alreadyShowing = this.bossBarTitles.containsKey(player.getName());
		this.bossBarTitles.put(player.getName(), title);
		this.bossBarValues.put(player.getName(), percent);
		
		if (alreadyShowing) {
			updateBar(player, title, percent);
		} else {
			showBar(player);
		}
	}
	
	@Override
	public void removePlayerBar(Player player) {
		if (this.bossBarTitles.remove(player.getName()) != null) {
			this.bossBarValues.remove(player.getName());
			MagicSpells.getVolatileCodeHandler().removeBossBar(player);
		}
	}
	
	private void showBar(Player player) {
		if (player == null) return;
		if (!player.isValid()) return;
		try {
			MagicSpells.getVolatileCodeHandler().setBossBar(player, this.bossBarTitles.get(player.getName()), this.bossBarValues.get(player.getName()));
		} catch (Exception e) {
			System.out.println("BOSS BAR EXCEPTION: " + e.getMessage());
		}
	}
	
	private void updateBar(Player player, String title, double val) {
		MagicSpells.getVolatileCodeHandler().updateBossBar(player, title, val);
	}
	
	@EventHandler
	public void onRespawn(final PlayerRespawnEvent event) {
		if (!this.bossBarTitles.containsKey(event.getPlayer().getName())) return;
		MagicSpells.scheduleDelayedTask(() -> showBar(event.getPlayer()), 10);
	}
	
	@EventHandler
	public void onTeleport(final PlayerTeleportEvent event) {
		if (!this.bossBarTitles.containsKey(event.getPlayer().getName())) return;
		MagicSpells.scheduleDelayedTask(() -> showBar(event.getPlayer()), 10);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		String playerName = event.getPlayer().getName();
		this.bossBarTitles.remove(playerName);
		this.bossBarValues.remove(playerName);
	}
	
	@Override
	public void turnOff() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!this.bossBarTitles.containsKey(player.getName())) continue;
			MagicSpells.getVolatileCodeHandler().removeBossBar(player);
		}
		this.bossBarTitles.clear();
		this.bossBarValues.clear();
	}
	
}
