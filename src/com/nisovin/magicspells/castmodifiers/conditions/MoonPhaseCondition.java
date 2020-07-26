package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class MoonPhaseCondition extends Condition {

	String phaseName = "";

	@Override
	public boolean setVar(String var) {
		phaseName = var.toLowerCase();
		return true;
	}
	
	@Override
	public boolean check(Player player) {
		return check(player, player.getLocation());
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return check(player, target.getLocation());
	}
	
	@Override
	public boolean check(Player player, Location location) {
		long time = location.getWorld().getFullTime();
		int phase = (int)((time / 24000) % 8);
		// Check if the moon is "Full" or "New"
		if (phase == 0 && phaseName.equals("full")) return true;
		if (phase == 4 && phaseName.equals("new")) return true;
		// Include backwards compatability for servers that use the vague phases
		if ((phase == 1 || phase == 2 || phase == 3) && phaseName.equals("waning")) return true;
		if ((phase == 5 || phase == 6 || phase == 7) && phaseName.equals("waxing")) return true;
		// Specific phases https://minecraft.gamepedia.com/Moon#Phases
		if ((phase == 1) && phaseName.equals("waning gibbous")) return true;
		if ((phase == 2) && phaseName.equals("last quarter")) return true;
		if ((phase == 3) && phaseName.equals("waning crescent")) return true;
		if ((phase == 5) && phaseName.equals("waxing crescent")) return true;
		if ((phase == 6) && phaseName.equals("first quarter")) return true;
		if ((phase == 7) && phaseName.equals("waxing gibbous")) return true;
		return false;
	}
}
