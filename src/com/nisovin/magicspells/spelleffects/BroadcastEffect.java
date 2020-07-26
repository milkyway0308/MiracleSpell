package com.nisovin.magicspells.spelleffects;

import com.nisovin.magicspells.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

/**
 * BroadcastEffect<br>
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
 *             <code>message</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>range</code>
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>targeted</code>
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class BroadcastEffect extends SpellEffect {

	String message = "";
	
	int range = 0;
	
	int rangeSq = 0;
	
	boolean targeted = false;
	
	@Override
	public void loadFromString(String string) {
		message = string;
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		message = config.getString("message", message);
		range = config.getInt("range", range);
		rangeSq = range * range;
		targeted = config.getBoolean("targeted", targeted);
	}

	@Override
	public Runnable playEffectLocation(Location location) {
		broadcast(location, message);
		return null;
	}
	
	@Override
	public Runnable playEffectEntity(Entity entity) {
		if (targeted) {
			if (entity instanceof Player) MagicSpells.sendMessage(message, (Player)entity, null);
		} else {
			String msg = message;
			if (entity instanceof Player) {
				msg = msg.replace("%a", ((Player)entity).getDisplayName())
					.replace("%t", ((Player)entity).getDisplayName())
					.replace("%n", entity.getName());
			}
			broadcast(entity == null ? null : entity.getLocation(), msg);
		}
		return null;
	}
	
	private void broadcast(Location location, String message) {
		if (range <= 0) {
			Util.forEachPlayerOnline(player -> MagicSpells.sendMessage(message, player, null));
		} else if (location != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) <= rangeSq) MagicSpells.sendMessage(message, player, null);
			}
		}
	}

}
