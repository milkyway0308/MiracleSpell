package com.nisovin.magicspells.util;

import org.bukkit.entity.EntityType;

public class V1_11EntityTypeHandler {
	
	private static boolean initialized = false;
	private static boolean v_1_11API = false;
	
	private static void initialize() {
		if (initialized) return;
		
		EntityType type = null;
		try {
			type = EntityType.valueOf("STRAY");
		} catch (Throwable ignored) {
			// No op
		}
		if (type != null) v_1_11API = true;
		
		initialized = true;
	}
	
	public static boolean newEntityTypesPresent() {
		initialize();
		return v_1_11API;
	}
	
}
