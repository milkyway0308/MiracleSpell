package com.nisovin.magicspells.util;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;

import com.nisovin.magicspells.MagicSpells;

public class MagicLocation {
	
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //
	
	private String world;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public MagicLocation(String world, int x, int y, int z) {
		this(world, x, y, z, 0, 0);
	}
	
	public MagicLocation(Location l) {
		this(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
	}
	
	public MagicLocation(String world, double x, double y, double z, float yaw, float pitch) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	// -------------------------------------------- //
	// ACCESS
	// -------------------------------------------- //
	
	public Location getLocation() {
		World realWorld = MagicSpells.plugin.getServer().getWorld(world);
		if (realWorld == null) return null;
		return new Location(realWorld, x, y, z, yaw, pitch);
	}
	
	public String getWorld() {
		return this.world;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	public double getZ() {
		return this.z;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
	// -------------------------------------------- //
	// HASHCODE
	// -------------------------------------------- //
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.world,
			this.x,
			this.y,
			this.z,
			this.pitch,
			this.yaw
		);
	}
	
	// -------------------------------------------- //
	// EQUALS
	// -------------------------------------------- //
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MagicLocation) {
			MagicLocation loc = (MagicLocation)o;
			return loc.world.equals(this.world) && loc.x == this.x && loc.y == this.y && loc.z == this.z && loc.yaw == this.yaw && loc.pitch == this.pitch;
		} else if (o instanceof Location) {
			Location loc = (Location)o;
			if (!LocationUtil.isSameWorld(loc, this.world)) return false;
			if (loc.getX() != this.x) return false;
			if (loc.getY() != this.y) return false;
			if (loc.getZ() != this.z) return false;
			if (loc.getYaw() != this.yaw) return false;
			if (loc.getPitch() != this.pitch) return false;
			return true;
		}
		return false;
	}
	
}
