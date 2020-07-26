package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;

public class CustomNameCondition extends Condition {

	String name;
	boolean isVar;
	
	@Override
	public boolean setVar(String var) {
		if (var == null || var.isEmpty()) return false;
		if (name.contains("%var:")) {
			isVar = true;
		}
		name = ChatColor.translateAlternateColorCodes('&', var);
		return true;
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) return false;
		if (isVar != false) {
			this.name = MagicSpells.doArgumentAndVariableSubstitution(name, player, null);
			String n = target.getCustomName();
			return n != null && !n.isEmpty() && name.equalsIgnoreCase(n);
		} else {
			String n = target.getCustomName();
			this.name = name.replace("__"," ");
			return n != null && !n.isEmpty() && name.equalsIgnoreCase(n);
		}
	}
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
