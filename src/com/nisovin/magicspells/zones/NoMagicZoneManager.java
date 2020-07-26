package com.nisovin.magicspells.zones;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.zones.NoMagicZone.ZoneCheckResult;

public class NoMagicZoneManager {
	
	private Map<String, Class<? extends NoMagicZone>> zoneTypes;
	private Map<String, NoMagicZone> zones;
	private Set<NoMagicZone> zonesOrdered;

	public NoMagicZoneManager() {
		// Create zone types
		this.zoneTypes = new HashMap<>();
		this.zoneTypes.put("cuboid", NoMagicZoneCuboid.class);
		this.zoneTypes.put("worldguard", NoMagicZoneWorldGuard.class);
		this.zoneTypes.put("residence", NoMagicZoneResidence.class);
	}
	
	// DEBUG INFO: level 3, loaded no magic zone, zonename
	// DEBUG INFO: level 1, no magic zones loaded #
	public void load(MagicConfig config) {
		// Get zones
		this.zones = new HashMap<>();
		this.zonesOrdered = new TreeSet<>();
				
		Set<String> zoneNodes = config.getKeys("no-magic-zones");
		if (zoneNodes != null) {
			for (String node : zoneNodes) {
				ConfigurationSection zoneConfig = config.getSection("no-magic-zones." + node);
				
				// Check enabled
				if (!zoneConfig.getBoolean("enabled", true)) continue;
				
				// Get zone type
				String type = zoneConfig.getString("type", "");
				if (type.isEmpty()) {
					MagicSpells.error("Invalid no-magic zone type '" + type + "' on zone '" + node + '\'');
					continue;
				}
				
				Class<? extends NoMagicZone> clazz = this.zoneTypes.get(type);
				if (clazz == null) {
					MagicSpells.error("Invalid no-magic zone type '" + type + "' on zone '" + node + '\'');
					continue;
				}
				
				// Create zone
				NoMagicZone zone;
				try {
					zone = clazz.newInstance();
				} catch (Exception e) {
					MagicSpells.error("Failed to create no-magic zone '" + node + '\'');
					e.printStackTrace();
					continue;
				}
				zone.create(node, zoneConfig);
				this.zones.put(node, zone);
				this.zonesOrdered.add(zone);
				MagicSpells.debug(3, "Loaded no-magic zone: " + node);
			}
		}
		
		MagicSpells.debug(1, "No-magic zones loaded: " + this.zones.size());
	}
	
	public boolean willFizzle(Player player, Spell spell) {
		return willFizzle(player.getLocation(), spell);
	}
	
	public boolean willFizzle(Location location, Spell spell) {
		for (NoMagicZone zone : this.zonesOrdered) {
			ZoneCheckResult result = zone.check(location, spell);
			if (result == ZoneCheckResult.DENY) return true;
			if (result == ZoneCheckResult.ALLOW) return false;
		}
		return false;
	}
	
	public boolean inZone(Player player, String zoneName) {
		return inZone(player.getLocation(), zoneName);
	}
	
	public boolean inZone(Location loc, String zoneName) {
		NoMagicZone zone = this.zones.get(zoneName);
		return zone != null && zone.inZone(loc);
	}
	
	public void sendNoMagicMessage(Player player, Spell spell) {
		for (NoMagicZone zone : this.zonesOrdered) {
			ZoneCheckResult result = zone.check(player.getLocation(), spell);
			if (result != ZoneCheckResult.DENY) continue;
			MagicSpells.sendMessage(zone.getMessage(), player, null);
			return;
		}
	}
	
	public int zoneCount() {
		return this.zones.size();
	}
	
	public void addZoneType(String name, Class<? extends NoMagicZone> clazz) {
		this.zoneTypes.put(name, clazz);
	}
	
	public void turnOff() {
		this.zoneTypes.clear();
		this.zones.clear();
		this.zoneTypes = null;
		this.zones = null;
	}
	
}
