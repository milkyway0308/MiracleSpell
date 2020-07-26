package com.nisovin.magicspells.spelleffects;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.DebugHandler;

/**
 * SmokeEffect<br>
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
 *         	   <code>dir</code>
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
public class SmokeEffect extends SpellEffect {

	int dir = 4;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			try {
				dir = Integer.parseInt(string);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		dir = config.getInt("dir", dir);
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		location.getWorld().playEffect(location, Effect.SMOKE, dir);
		return null;
	}

}
