package com.nisovin.magicspells.spells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface TargetedEntitySpell {
	
	boolean castAtEntity(Player caster, LivingEntity target, float power);
	
	boolean castAtEntity(LivingEntity target, float power);
	
}
