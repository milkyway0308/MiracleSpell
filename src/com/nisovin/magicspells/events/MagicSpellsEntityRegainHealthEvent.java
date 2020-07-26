package com.nisovin.magicspells.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class MagicSpellsEntityRegainHealthEvent extends EntityRegainHealthEvent implements IMagicSpellsCompatEvent {

	public MagicSpellsEntityRegainHealthEvent(Entity entity, double amount, RegainReason regainReason) {
		super(entity, amount, regainReason);
	}

}
