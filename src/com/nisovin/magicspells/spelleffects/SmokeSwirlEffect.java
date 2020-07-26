package com.nisovin.magicspells.spelleffects;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;

/**
 * SmokeSwirlEffect<br>
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
public class SmokeSwirlEffect extends SpellEffect {

	int duration = TimeUtil.TICKS_PER_SECOND;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			try {
				duration = Integer.parseInt(string);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		duration = config.getInt("duration", duration);
	}

	int[] x = {1, 1, 0, -1, -1, -1, 0, 1};
	int[] z = {0, 1, 1, 1, 0, -1, -1, -1};
	int[] v = {7, 6, 3, 0, 1, 2, 5, 8};
	
	@Override
	public Runnable playEffectLocation(Location location) {		
		new Animator(location, 1, duration);
		return null;
	}
	
	@Override
	public Runnable playEffectEntity(Entity entity) {
		new Animator(entity, 1, duration);
		return null;
	}
	
	private class Animator implements Runnable {
		
		private Entity entity;
		private Location location;
		private int interval;
		private int animatorDuration;
		private int iteration;
		private int animatorTaskId;
		
		public Animator(Location location, int interval, int duration) {
			this(interval, duration);
			this.location = location;
		}
		
		public Animator(Entity entity, int interval, int duration) {
			this(interval, duration);
			this.entity = entity;
		}
		
		public Animator(int interval, int duration) {
			this.interval = interval;
			this.animatorDuration = duration;
			this.iteration = 0;
			this.animatorTaskId = MagicSpells.scheduleRepeatingTask(this, 0, interval);
		}
		
		@Override
		public void run() {
			if (iteration * interval > animatorDuration) {
				Bukkit.getScheduler().cancelTask(animatorTaskId);
			} else {
				int i = iteration % 8;
				Location loc;
				if (location != null) {
					loc = location;
				} else {
					loc = entity.getLocation();
				}
				loc.getWorld().playEffect(loc.clone().add(x[i], 0, z[i]), Effect.SMOKE, v[i]);
				iteration++;
			}
		}
		
	}
	
}
