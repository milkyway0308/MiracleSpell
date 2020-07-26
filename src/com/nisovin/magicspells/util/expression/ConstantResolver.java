package com.nisovin.magicspells.util.expression;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

public class ConstantResolver extends ValueResolver {

	private Number value;
	
	public ConstantResolver(Number value) {
		MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Creating constant resolver with input of " + value.toString() + "; " + value.doubleValue());
		this.value = value;
	}
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		return value;
	}

}
