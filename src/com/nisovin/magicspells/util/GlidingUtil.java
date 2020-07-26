package com.nisovin.magicspells.util;

import org.bukkit.entity.LivingEntity;

public class GlidingUtil {

	private static boolean initialized = false;
	private static boolean enabled = false;
	
	private static boolean initialize(LivingEntity livingEntity) {
		if (livingEntity == null) return enabled;
		if (initialized) return enabled;
		try {
			boolean ret = livingEntity.isGliding();
			enabled = true;
			return ret;
		} catch (Exception e) {
			return false;
		} finally {
			initialized = true;
		}
	}
	
	public static boolean isGliding(LivingEntity livingEntity) {
		if (livingEntity == null) return false;
		if (!initialized) initialize(livingEntity);
		if (!enabled) return false;
		return livingEntity.isGliding();
	}
	
}
