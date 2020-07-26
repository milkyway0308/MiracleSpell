package com.nisovin.magicspells.spells;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TargetedLocationSpell {
	
	boolean castAtLocation(Player caster, Location target, float power);

	boolean castAtLocation(Location target, float power);
	
}
