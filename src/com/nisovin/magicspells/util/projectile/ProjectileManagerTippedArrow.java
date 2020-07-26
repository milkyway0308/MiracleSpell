package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.TippedArrow;

public class ProjectileManagerTippedArrow extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return TippedArrow.class;
	}
	
}
