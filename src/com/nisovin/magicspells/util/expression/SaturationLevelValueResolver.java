package com.nisovin.magicspells.util.expression;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SaturationLevelValueResolver extends ValueResolver {

	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		if (player != null) {
			return player.getSaturation();
		}
		Player p = Bukkit.getServer().getPlayer(playerName);
		if (p != null) {
			return p.getSaturation();
		}
		return 0;
	}

}
