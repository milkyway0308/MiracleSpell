package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ParticleEffect;

/**
 * AngryEffect<br>
 * <table border=1>
 *     	<tr>
 *         <th>
 *             Config Field
 *         </th>
 *    	     <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     	</tr>
 *     	<tr>
 *         	<td>
 *         		<code>range</code>
 *         	</td>
 *         	<td>
 *         		Integer
 *         	</td>
 *         	<td>
 *         		How far the effect should be visible.
 *         	</td>
 *     	</tr>
 * </table>
 */
public class AngryEffect extends SpellEffect {

	private ParticleEffect effect = ParticleEffect.VILLAGER_ANGRY;
	
	private double range = 32;
	
	// These are location shifts made to the center point
	private float xOffset = 0;
	private float yOffset = 2;
	private float zOffset = 0;
	
	// These are about how far particles can be from the center
	private float offsetX = 0;
	private float offsetY = 0;
	private float offsetZ = 0;
	
	private float speed = .2F;
	private int count = 1;
	
	@Override
	public void loadFromString(String string) {
		// TODO make a string loading schema
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		range = config.getDouble("range", range);
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		//MagicSpells.getVolatileCodeHandler().playParticleEffect(location, "angryVillager", 0F, 0F, .2F, 1, 32, 2F);
		//Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset
		
		//ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount
		effect.display(null, location.clone().add(xOffset, yOffset, zOffset), null, range, offsetX, offsetY, offsetZ, speed, count);
		return null;
	}
	
}
