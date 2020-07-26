package com.nisovin.magicspells.zones;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class NoMagicZoneCuboid extends NoMagicZone {
	
	private String worldName;
	private int minx;
	private int miny;
	private int minz;
	private int maxx;
	private int maxy;
	private int maxz;
	
	@Override
	public void initialize(ConfigurationSection config) {
		this.worldName = config.getString("world", "");
		
		String[] p1 = config.getString("point1", "0,0,0").replace(" ", "").split(",");
		String[] p2 = config.getString("point2", "0,0,0").replace(" ", "").split(",");
		int x1 = Integer.parseInt(p1[0]);
		int y1 = Integer.parseInt(p1[1]);
		int z1 = Integer.parseInt(p1[2]);
		int x2 = Integer.parseInt(p2[0]);
		int y2 = Integer.parseInt(p2[1]);
		int z2 = Integer.parseInt(p2[2]);
		
		if (x1 < x2) {
			this.minx = x1;
			this.maxx = x2;
		} else {
			this.minx = x2;
			this.maxx = x1;
		}
		if (y1 < y2) {
			this.miny = y1;
			this.maxy = y2;
		} else {
			this.miny = y2;
			this.maxy = y1;
		}
		if (z1 < z2) {
			this.minz = z1;
			this.maxz = z2;
		} else {
			this.minz = z2;
			this.maxz = z1;
		}
	}

	@Override
	public boolean inZone(Location location) {
		if (!this.worldName.equalsIgnoreCase(location.getWorld().getName())) return false;
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		return this.minx <= x && x <= this.maxx && this.miny <= y && y <= this.maxy && this.minz <= z && z <= this.maxz;
	}
	
}
