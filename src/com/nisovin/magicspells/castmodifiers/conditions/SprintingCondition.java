package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class SprintingCondition extends Condition {
	
	@Override
	public boolean setVar(String var) {
		return true;
	}
	
	@Override
	public boolean check(Player player) {
		return player.isSprinting();
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
