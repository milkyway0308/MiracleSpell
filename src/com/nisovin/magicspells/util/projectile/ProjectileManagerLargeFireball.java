package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Projectile;

public class ProjectileManagerLargeFireball extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return LargeFireball.class;
	}
	
}
