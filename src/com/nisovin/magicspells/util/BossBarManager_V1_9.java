package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager_V1_9 implements BossBarManager {

	Map<String, BossBar> bars = new HashMap<>();
	
	@Override
	public void setPlayerBar(Player player, String title, double percent) {
		BossBar bar = this.bars.get(player.getName());
		if (bar == null) {
			bar = Bukkit.createBossBar(ChatColor.translateAlternateColorCodes('&', title), BarColor.PURPLE, BarStyle.SOLID);
			this.bars.put(player.getName(), bar);
		}
		bar.setTitle(ChatColor.translateAlternateColorCodes('&', title));
		bar.setProgress(percent);
		bar.addPlayer(player);
	}

	@Override
	public void removePlayerBar(Player player) {
		BossBar bar = this.bars.remove(player.getName());
		if (bar != null) bar.removeAll();
	}

	@Override
	public void turnOff() {
		this.bars.values().forEach(BossBar::removeAll);
		this.bars.clear();
	}

}
