package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class UpTimeCondition extends Condition {

	static long startTime = System.currentTimeMillis();
	
	int ms;
	
	@Override
	public boolean setVar(String var) {
		try {
			ms = Integer.parseInt(var) * (int)TimeUtil.MILLISECONDS_PER_SECOND;
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return System.currentTimeMillis() > startTime + ms;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return System.currentTimeMillis() > startTime + ms;
	}

	@Override
	public boolean check(Player player, Location location) {
		return System.currentTimeMillis() > startTime + ms;
	}

}
