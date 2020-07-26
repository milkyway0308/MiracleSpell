package com.nisovin.magicspells.castmodifiers.conditions;

import java.util.regex.Pattern;

import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class NamePatternCondition extends Condition {

	String rawPattern;
	private Pattern compiledPattern;
	
	@Override
	public boolean setVar(String var) {
		if (var == null || var.isEmpty()) return false;
		rawPattern = var;
		compiledPattern = Pattern.compile(rawPattern);
		// note, currently won't translate the & to the color code,
		// this will need to be done through regex unicode format 
		return true;
	}

	@Override
	public boolean check(Player player) {
		return RegexUtil.matches(compiledPattern, player.getName()) || RegexUtil.matches(compiledPattern, player.getDisplayName());
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) return check((Player)target);
		String n = target.getCustomName();
		return n != null && !n.isEmpty() && RegexUtil.matches(compiledPattern, n);
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
