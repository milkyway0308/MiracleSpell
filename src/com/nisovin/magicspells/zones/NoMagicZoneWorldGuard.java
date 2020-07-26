package com.nisovin.magicspells.zones;

import com.nisovin.magicspells.util.compat.CompatBasics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class NoMagicZoneWorldGuard extends NoMagicZone {

	private String worldName;
	private String regionName;
	private ProtectedRegion region;
	
	@Override
	public void initialize(ConfigurationSection config) {
		this.worldName = config.getString("world", "");
		this.regionName = config.getString("region", "");
	}

	@Override
	public boolean inZone(Location location) {
		// Check world
		if (!this.worldName.equals(location.getWorld().getName())) return false;
		
		// Get region, if necessary
		if (this.region == null) {
			WorldGuardPlugin worldGuard = null;
			if (CompatBasics.pluginEnabled("WorldGuard")) worldGuard = (WorldGuardPlugin)CompatBasics.getPlugin("WorldGuard");
			if (worldGuard != null) {
				World w = Bukkit.getServer().getWorld(this.worldName);
				if (w != null) {
					RegionManager rm = worldGuard.getRegionManager(w);
					if (rm != null) this.region = rm.getRegion(this.regionName);
				}
			}
		}
		
		// Check if contains
		if (this.region != null) {
			com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
			return this.region.contains(v);
		}
		MagicSpells.error("Failed to access WorldGuard region '" + this.regionName + '\'');
		return false;
	}

}
