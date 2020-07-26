package com.nisovin.magicspells.util.expression;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FoodLevelValueResolver extends ValueResolver {

	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		if (player != null) {
			return player.getFoodLevel();
		}
		Player p = Bukkit.getServer().getPlayer(playerName);
		if (p != null) {
			return p.getFoodLevel();
		}
		return 0;
	}

}
