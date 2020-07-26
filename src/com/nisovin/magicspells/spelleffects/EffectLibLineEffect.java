package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class EffectLibLineEffect extends EffectLibEffect {
	
	//force static origin location
	boolean forceStaticOriginLocation = true;
	
	//force static target location
	boolean forceStaticTargetLocation = false;
	
	
	@Override
	public void loadFromConfig(ConfigurationSection section) {
		super.loadFromConfig(section);
		forceStaticOriginLocation = section.getBoolean("static-origin-location", forceStaticOriginLocation);
		forceStaticTargetLocation = section.getBoolean("static-target-location", forceStaticTargetLocation);
	}
	
	@Override
	public Runnable playEffect(Location location1, Location location2) {
		manager.start(className, effectLibSection, location1, location2, null, null, null);
		return null;
	}
	
	@Override
	public void playTrackingLinePatterns(Location origin, Location target, Entity originEntity,
			Entity targetEntity) {
		if (forceStaticOriginLocation) {
			if (origin == null && originEntity != null) origin = originEntity.getLocation();
			originEntity = null;
		}
		if (forceStaticTargetLocation) {
			if (target == null && targetEntity != null) target = targetEntity.getLocation();
			targetEntity = null;
		}
		manager.start(className, effectLibSection, origin, target, originEntity, targetEntity, null);
	}
	
}
