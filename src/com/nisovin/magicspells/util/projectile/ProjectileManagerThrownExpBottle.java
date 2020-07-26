package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;

public class ProjectileManagerThrownExpBottle extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return ThrownExpBottle.class;
	}
	
}
