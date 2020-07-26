package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class ElevationBelowCondition extends Condition {
	
	double y;
	
	@Override
	public boolean setVar(String var) {
		try {
			y = Double.parseDouble(var);
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return player.getLocation().getY() <= y;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return target.getLocation().getY() <= y;
	}

	@Override
	public boolean check(Player player, Location location) {
		return location.getY() <= y;
	}
	
}
