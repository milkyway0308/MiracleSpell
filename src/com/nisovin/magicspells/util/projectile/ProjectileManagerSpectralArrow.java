package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;

public class ProjectileManagerSpectralArrow extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return SpectralArrow.class;
	}
	
}
