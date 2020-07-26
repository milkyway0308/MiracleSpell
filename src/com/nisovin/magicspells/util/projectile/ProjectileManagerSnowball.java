package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;

public class ProjectileManagerSnowball extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return Snowball.class;
	}
	
}
