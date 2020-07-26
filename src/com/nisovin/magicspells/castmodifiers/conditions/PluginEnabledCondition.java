package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.util.compat.CompatBasics;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class PluginEnabledCondition extends Condition {

	private String pluginName = null;
	
	@Override
	public boolean setVar(String var) {
		if (var == null) return false;
		var = var.trim();
		if (var.isEmpty()) return false;
		this.pluginName = var;
		return true;
	}

	@Override
	public boolean check(Player player) {
		return check();
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return check();
	}

	@Override
	public boolean check(Player player, Location location) {
		return check();
	}
	
	private boolean check() {
		if (pluginName == null) return false;
		return CompatBasics.pluginEnabled(this.pluginName);
	}

}
