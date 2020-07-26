package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.PlayerNameUtils;

public class PlayerOnlineCondition extends Condition {
	
	String name;
	
	@Override
	public boolean setVar(String var) {
		name = var;
		return true;
	}
	
	@Override
	public boolean check(Player player) {
		return PlayerNameUtils.getPlayerExact(name) != null;
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return check(player);
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return check(player);
	}

}
