package com.nisovin.magicspells.events;

import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class MagicSpellsExplosionPrimeEvent extends ExplosionPrimeEvent implements IMagicSpellsCompatEvent {

	public MagicSpellsExplosionPrimeEvent(Explosive explosive) {
		super(explosive);
		// TODO Auto-generated constructor stub
	}

}
