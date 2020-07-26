package com.nisovin.magicspells.util.expression;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class ValueResolver {
	
	public abstract Number resolveValue(String playerName, Player player, Location loc1, Location loc2);
	
}
