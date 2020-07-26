package com.nisovin.magicspells.util.projectile;

import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.Projectile;

public class ProjectileManagerLingeringPotion extends ProjectileManager {
	
	@Override
	public Class<? extends Projectile> getProjectileClass() {
		return LingeringPotion.class;
	}
	
}
