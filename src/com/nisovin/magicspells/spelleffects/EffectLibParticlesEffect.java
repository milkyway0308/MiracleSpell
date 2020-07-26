package com.nisovin.magicspells.spelleffects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ParticleEffect;
import de.slikey.effectlib.util.ParticleEffect.ItemData;
import de.slikey.effectlib.util.ParticleEffect.ParticleData;

public class EffectLibParticlesEffect extends SpellEffect {

	String name = "";
	float offsetX = 1;
	float offsetY = 1;
	float offsetZ = 1;
	float speed = 1;
	int amount = 16;
	double range = 32;
	String colorString;

	private ParticleEffect effect;

	private ParticleEffect defaultEffect = ParticleEffect.SPELL_MOB;
	private Color color = null;
	private ParticleData data = null;

	@Override
	public void loadFromString(String string) {
		//TODO make a string loading schema
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		//name
		//float offsetX
		//float offsetY
		//float offsetZ
		//float speed
		//int amount
		//double range

		name = config.getString("particle-name", name);
		offsetX = (float) config.getDouble("offset-x", offsetX);
		offsetY = (float) config.getDouble("offset-y", offsetZ);
		offsetZ = (float) config.getDouble("offset-z", offsetZ);
		speed = (float)config.getDouble("speed", speed);
		amount = config.getInt("amount", amount);
		range = config.getDouble("range", range);
		if (name != null) effect = ParticleEffect.fromName(name);
		
		if (effect == null) effect = ParticleEffect.valueOf(name);
		
		if (effect == null) effect = defaultEffect;
		colorString = config.getString("color", null);
		if (colorString != null) {
			String[] colorData = colorString.split(",");
			if (colorData.length >= 3) {
				int red = Integer.parseInt(colorData[0]);
				int green = Integer.parseInt(colorData[1]);
				int blue = Integer.parseInt(colorData[2]);
				color = Color.fromRGB(red, green, blue);
			}
		}

		if (config.contains("data")) {
			if (config.isConfigurationSection("data")) {
				ConfigurationSection dataSection = config.getConfigurationSection("data");
				String materialString = dataSection.getString("material", null);
				if (materialString != null) {
					Material material = Material.getMaterial(materialString);
					if (material != null) {
						String byteDataRaw = config.getString("byte-data", null);
						if (byteDataRaw != null) {
							byte data;
							data = Byte.parseByte(byteDataRaw);
							this.data = new ItemData(material, data);
						}
					}
				}

			}
		}
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		//ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount
		effect.display(data, location, color, range, offsetX, offsetY, offsetZ, speed, amount);
		return null;
	}

}
