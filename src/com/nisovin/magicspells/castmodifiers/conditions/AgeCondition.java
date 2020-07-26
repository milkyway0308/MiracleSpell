package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class AgeCondition extends Condition {
	
	private boolean passBaby = false;
	private boolean passAdult = false;
	
	@Override
	public boolean setVar(String var) {
		if (var != null) {
			if (var.equalsIgnoreCase("baby")) {
				passBaby = true;
				return true;
			} else if (var.equalsIgnoreCase("adult")) {
				passAdult = true;
				return true;
			}
		}
		passBaby = true;
		passAdult = true;
		return true;
	}

	@Override
	public boolean check(Player player) {
		// Always false for this since player aren't instance of Ageable
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Ageable) {
			boolean adult = ((Ageable) target).isAdult();
			return adult ? passAdult : passBaby;
		}
		return false;
	}

	@Override
	public boolean check(Player player, Location location) {
		// Locations aren't Ageable
		return false;
	}

}
