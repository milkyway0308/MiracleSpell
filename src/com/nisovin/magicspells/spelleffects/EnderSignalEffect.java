package com.nisovin.magicspells.spelleffects;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class EnderSignalEffect extends SpellEffect {

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
		location.getWorld().playEffect(location, Effect.ENDER_SIGNAL, 0);
		return null;
	}

}
