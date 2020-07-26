package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;

public class DragonDeathEffect extends SpellEffect {

	@Override
	public void loadFromString(String string) {
		//TODO make a string loading schema
	}

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		//TODO make a config loading schema
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		MagicSpells.getVolatileCodeHandler().playDragonDeathEffect(location);
		return null;
	}

}
