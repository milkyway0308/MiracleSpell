package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;

public class ProjectileManagerShulkerBullet extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return ShulkerBullet.class;
	}
	
}
