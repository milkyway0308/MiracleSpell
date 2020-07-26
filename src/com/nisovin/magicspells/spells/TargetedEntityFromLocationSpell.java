package com.nisovin.magicspells.spells;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface TargetedEntityFromLocationSpell {
	
	boolean castAtEntityFromLocation(Player caster, Location from, LivingEntity target, float power);
	
	boolean castAtEntityFromLocation(Location from, LivingEntity target, float power);

}
