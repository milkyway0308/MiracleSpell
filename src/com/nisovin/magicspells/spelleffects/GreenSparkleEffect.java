package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ParticleEffect;

public class GreenSparkleEffect extends SpellEffect {

	private ParticleEffect effect = ParticleEffect.VILLAGER_HAPPY;
	
	private float xOffset = 0;
	private float yOffset = 2F;
	private float zOffset = 0;
	
	private double range = 32;
	private int count = 4;
	
	private float speed = .5F;
	private float spreadHoriz = .3F;
	private float spreadVert = .3F;
	
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
		//MagicSpells.getVolatileCodeHandler().playParticleEffect(location, "happyVillager", .3F, .3F, .5F, 4, 32, 2F);
		//Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset
		
		effect.display(null, location.clone().add(xOffset, yOffset, zOffset), null, range, spreadHoriz, spreadVert, spreadHoriz, speed, count);
		//ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount
		return null;
	}
	
}
