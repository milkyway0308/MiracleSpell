package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;

public class ProjectileManagerWitherSkull extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return WitherSkull.class;
	}
	
}
