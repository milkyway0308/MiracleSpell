package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class ExplosionEffect extends SpellEffect {

	@Override
	public void loadFromString(String string) {
		// TODO make a string loading schema
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		// TODO make a config loading schema
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		location.getWorld().createExplosion(location, 0F);
		return null;
	}

}
