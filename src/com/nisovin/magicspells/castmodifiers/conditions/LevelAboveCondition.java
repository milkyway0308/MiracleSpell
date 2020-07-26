package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class LevelAboveCondition extends Condition {

	int level = 0;

	@Override
	public boolean setVar(String var) {
		try {
			level = Integer.parseInt(var);
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}
	
	@Override
	public boolean check(Player player) {
		return player.getLevel() > level;
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return target instanceof Player && check((Player)target);
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
