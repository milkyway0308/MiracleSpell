package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BooleanUtils;
import com.nisovin.magicspells.util.ConfigData;

/**
 * public class FireworksEffect<p>
 * Configuration fields:<br>
 * <ul>
 * <li>flicker</li>
 * <li>trail</li>
 * <li>type</li>
 * <li>flight</li>
 * <li>colors</li>
 * <li>fade-colors</li>
 * </ul>
 */
public class FireworksEffect extends SpellEffect {

	@ConfigData(field="flicker", dataType="boolean", defaultValue="false")
	boolean flicker = false;
	
	@ConfigData(field="trail", dataType="boolean", defaultValue="false")
	boolean trail = false;
	
	@ConfigData(field="type", dataType="int", defaultValue="0")
	int type = 0;
	
	@ConfigData(field="colors", dataType="String", defaultValue="FF0000")
	int[] colors = new int[] { 0xFF0000 };
	
	@ConfigData(field="fade-colors", dataType="String", defaultValue="")
	int[] fadeColors = new int[] { 0xFF0000 };
	
	@ConfigData(field="flight", dataType="int", defaultValue="0")
	int flightDuration = 0;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			String[] data = string.split(" ");
			if (data.length >= 1 && BooleanUtils.isYes(data[0])) flicker = true;
			if (data.length >= 2 && BooleanUtils.isYes(data[1])) trail = true;
			if (data.length >= 3) type = Integer.parseInt(data[2]);
			if (data.length >= 4) {
				String[] c = data[3].split(",");
				colors = new int[c.length];
				for (int i = 0; i < c.length; i++) {
					colors[i] = Integer.parseInt(c[i], 16);
				}
			}
			if (data.length >= 5) {
				String[] c = data[4].split(",");
				fadeColors = new int[c.length];
				for (int i = 0; i < c.length; i++) {
					fadeColors[i] = Integer.parseInt(c[i], 16);
				}
			}
			if (data.length >= 6) flightDuration = Integer.parseInt(data[5]);
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		flicker = config.getBoolean("flicker", false);
		trail = config.getBoolean("trail", false);
		type = config.getInt("type", type);
		flightDuration = config.getInt("flight", flightDuration);
		
		String[] c = config.getString("colors", "FF0000").replace(" ", "").split(",");
		if (c.length > 0) {
			colors = new int[c.length];
			for (int i = 0; i < colors.length; i++) {
				try {
					colors[i] = Integer.parseInt(c[i], 16);
				} catch (NumberFormatException e) {
					colors[i] = 0;
				}
			}
		}
		
		String[] fc = config.getString("fade-colors", "").replace(" ", "").split(",");
		if (fc.length > 0) {
			fadeColors = new int[fc.length];
			for (int i = 0; i < fadeColors.length; i++) {
				try {
					fadeColors[i] = Integer.parseInt(fc[i], 16);
				} catch (NumberFormatException e) {
					fadeColors[i] = 0;
				}
			}
		}
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		MagicSpells.getVolatileCodeHandler().createFireworksExplosion(location, flicker, trail, type, colors, fadeColors, flightDuration);
		return null;
	}
	
}
