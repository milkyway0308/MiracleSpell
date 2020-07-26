package com.nisovin.magicspells.util.expression;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLocationYValueResolver extends ValueResolver {
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		if (player != null) {
			return player.getLocation().getY();
		}
		Player p = Bukkit.getServer().getPlayer(playerName);
		if (p != null) {
			return p.getLocation().getY();
		}
		
		if (loc1 != null) {
			return loc1.getY();
		}
		
		if (loc2 != null) {
			return loc2.getY();
		}
		
		return 0;
	}

}
