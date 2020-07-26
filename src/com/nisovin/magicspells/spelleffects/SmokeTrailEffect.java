package com.nisovin.magicspells.spelleffects;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;

/**
 * SmokeTrailEffect<br>
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
 *             <code>interval</code>
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
public class SmokeTrailEffect extends SpellEffect {

	int interval = 0;
	
	@Override
	public void loadFromString(String string) {
		if (string != null) {
			try {
				interval = Integer.parseInt(string);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		interval = config.getInt("interval", interval);
	}

	@Override
	public Runnable playEffect(Location location1, Location location2) {		
		SmokeStreamEffect effect = new SmokeStreamEffect(location1, location2);
		if (interval > 0) {
			effect.start(interval);
		} else {
			effect.showNoAnimation();
		}
		return null;
	}
	
	// Thanks to DrBowe for sharing the code
	private class SmokeStreamEffect implements Runnable {
		
		private Location startLoc;
		private Location endLoc;
		private ArrayList<Location> locationsForProjection;
		private World world;

		private int i;
		private int id;
		
		public SmokeStreamEffect(Location loc1, Location loc2) {
			this.startLoc = loc1;
			this.endLoc = loc2;
			this.world = startLoc.getWorld();

			this.locationsForProjection = calculateLocsForProjection();
			this.i = 0;
		}
		
		public void start(int interval) {
			this.id = MagicSpells.scheduleRepeatingTask(this, interval, interval);
		}

		public void showNoAnimation() {
			while (this.i < locationsForProjection.size()) {
				run();
			}
		}
		
		@Override
		public void run() {
			if (i > locationsForProjection.size() - 1) {
				MagicSpells.cancelTask(id);
				return;
			}
			Location loc = locationsForProjection.get(i);
			for (int j = 0; j <= 8; j += 2) {
				world.playEffect(loc, Effect.SMOKE, j);
			}
			i++;			
		}
		
		private ArrayList<Location> calculateLocsForProjection() {
			double x1;
			double y1;
			double z1;
			double x2;
			double y2;
			double z2;
			double xVect;
			double yVect;
			double zVect;
			x1 = endLoc.getX();
			y1 = endLoc.getY();
			z1 = endLoc.getZ();
			x2 = startLoc.getX();
			y2 = startLoc.getY();
			z2 = startLoc.getZ();
			xVect = x2 - x1;
			yVect = y2 - y1;
			zVect = z2 - z1;
			double distance = startLoc.distance(endLoc);
			ArrayList<Location> tmp = new ArrayList<>((int)Math.floor(distance));
			
			for (double t = 0; t <= 1; t += 1/distance) {
				tmp.add(new Location(world, x2 - (xVect * t), y2 - (yVect * t) + 1, z2 - (zVect * t)));
			}
			return tmp;
		}
		
	}
	
}
