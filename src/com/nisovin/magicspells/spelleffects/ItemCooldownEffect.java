package com.nisovin.magicspells.spelleffects;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.Util;

/**
 * ItemCooldownEffect<br>
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
 *             <code>item</code>
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
public class ItemCooldownEffect extends SpellEffect {

	ItemStack item;
	int duration;
	
	@Override
	public void loadFromString(String string) {
		String[] split = Util.splitParams(string);
		item = Util.getItemStackFromString(split[0]);
		duration = Integer.parseInt(split[1]);
	}

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		item = Util.getItemStackFromString(config.getString("item", "stone"));
		duration = config.getInt("duration", TimeUtil.TICKS_PER_SECOND);
	}
	
	@Override
	protected Runnable playEffectEntity(Entity entity) {
		if (!(entity instanceof Player)) return null;
		MagicSpells.getVolatileCodeHandler().showItemCooldown((Player)entity, item, duration);
		return null;
	}
	
}
