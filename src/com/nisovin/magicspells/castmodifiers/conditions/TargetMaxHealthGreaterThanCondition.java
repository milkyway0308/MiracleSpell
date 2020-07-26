package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class TargetMaxHealthGreaterThanCondition extends Condition {

	double level;
	
	@Override
	public boolean setVar(String var) {
		try {
			level = Double.parseDouble(var);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return target.getMaxHealth() > level;
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
