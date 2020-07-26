package com.nisovin.magicspells.spelleffects;

import org.bukkit.entity.Entity;

public class EffectLibEntityEffect extends EffectLibEffect {
	
	@Override
	protected Runnable playEffectEntity(final Entity e) {
		return manager.start(className, effectLibSection, e);
	}

}
