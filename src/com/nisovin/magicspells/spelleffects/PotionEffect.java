package com.nisovin.magicspells.spelleffects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;

/**
 * PotionEffect<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>color</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             Color of the potion effect using hexidecimal color format
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>duration</code>
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class PotionEffect extends SpellEffect {
	
	int color = 0xFF0000;
	
	int duration = 30;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			String[] data = string.split(" ");
			try {
				color = Integer.parseInt(data[0], 16);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
			if (data.length > 1) {
				try {
					duration = Integer.parseInt(data[1]);
				} catch (NumberFormatException e) {
					DebugHandler.debugNumberFormat(e);
				}
			}
		}
	}
	
	@Override
	public void loadFromConfig(ConfigurationSection config) {
		String c = config.getString("color", "");
		if (!c.isEmpty()) {
			try {
				color = Integer.parseInt(c, 16);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
		}
		duration = config.getInt("duration", duration);
	}

	@Override
	public Runnable playEffectEntity(Entity entity) {
		if (!(entity instanceof LivingEntity)) return null;
		LivingEntity le = (LivingEntity)entity;
		MagicSpells.getVolatileCodeHandler().addPotionGraphicalEffect(le, color, duration);
		return null;
	}
	
}
