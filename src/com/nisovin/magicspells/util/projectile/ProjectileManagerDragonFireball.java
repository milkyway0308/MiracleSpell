package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Projectile;

public class ProjectileManagerDragonFireball extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return DragonFireball.class;
	}
	
}
