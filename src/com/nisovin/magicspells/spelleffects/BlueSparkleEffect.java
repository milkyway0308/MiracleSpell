package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ParticleEffect;

public class BlueSparkleEffect extends SpellEffect {

	private ParticleEffect effect = ParticleEffect.SPELL_WITCH;
	
	private double range = 32;
	private float spreadHoriz = .2F;
	private float spreadVert = .2F;
	private float speed = .1F;
	private int count = 20;
	private float xOffset = 0;
	private float yOffset = 2F;
	private float zOffset = 0;
	
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
		//MagicSpells.getVolatileCodeHandler().playParticleEffect(location, "witchMagic", .2F, .2F, .1F, 20, 32, 2F);
		//Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset
		
		effect.display(null, location.clone().add(xOffset, yOffset, zOffset), null, range, spreadHoriz, spreadVert, spreadHoriz, speed, count);
		//ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount
		return null;
	}
	
}
