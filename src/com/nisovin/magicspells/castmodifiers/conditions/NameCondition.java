package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class NameCondition extends Condition {

	String name;
	
	@Override
	public boolean setVar(String var) {
		if (var == null || var.isEmpty()) return false;
		name = ChatColor.translateAlternateColorCodes('&', var);
		return true;
	}

	@Override
	public boolean check(Player player) {
		return player.getName().equalsIgnoreCase(name) || player.getDisplayName().equalsIgnoreCase(name);
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) return check((Player)target);
		String n = target.getCustomName();
		return n != null && !n.isEmpty() && name.equalsIgnoreCase(n);
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
