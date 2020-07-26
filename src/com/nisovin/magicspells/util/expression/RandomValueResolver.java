package com.nisovin.magicspells.util.expression;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RandomValueResolver extends ValueResolver {
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		return Math.random();
	}

}
