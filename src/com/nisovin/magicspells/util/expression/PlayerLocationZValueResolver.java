package com.nisovin.magicspells.util.expression;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLocationZValueResolver extends ValueResolver {
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		if (player != null) {
			return player.getLocation().getZ();
		}
		Player p = Bukkit.getServer().getPlayer(playerName);
		if (p != null) {
			return p.getLocation().getZ();
		}
		
		if (loc1 != null) {
			return loc1.getZ();
		}
		
		if (loc2 != null) {
			return loc2.getZ();
		}
		
		return 0;
	}

}
