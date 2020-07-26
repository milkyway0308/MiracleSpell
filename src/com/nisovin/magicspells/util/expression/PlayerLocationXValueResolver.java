package com.nisovin.magicspells.util.expression;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLocationXValueResolver extends ValueResolver {
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		if (player != null) {
			return player.getLocation().getX();
		}
		Player p = Bukkit.getServer().getPlayer(playerName);
		if (p != null) {
			return p.getLocation().getX();
		}
		
		if (loc1 != null) {
			return loc1.getX();
		}
		
		if (loc1 != null) {
			return loc1.getX();
		}
		
		return 0;
	}

}
