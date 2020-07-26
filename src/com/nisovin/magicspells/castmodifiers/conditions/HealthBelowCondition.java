package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class HealthBelowCondition extends Condition {
	
	int health = 0;
	boolean percent = false;

	@Override
	public boolean setVar(String var) {
		try {
			if (var.endsWith("%")) {
				percent = true;
				var = var.replace("%", "");
			}
			health = Integer.parseInt(var);
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}
	
	@Override
	public boolean check(Player player) {
		return check(player, player);
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (percent) return target.getHealth() / target.getMaxHealth() * 100 < health;
		return target.getHealth() < health;
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
