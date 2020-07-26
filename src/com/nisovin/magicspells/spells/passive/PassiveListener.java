package com.nisovin.magicspells.spells.passive;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.spells.PassiveSpell;

public abstract class PassiveListener implements Listener {

	EventPriority priority;
	
	public EventPriority getEventPriority() {
		return priority;
	}
		
	public static boolean cancelDefaultAction(PassiveSpell spell, boolean casted) {
		if (casted && spell.cancelDefaultAction()) return true;
		if (!casted && spell.cancelDefaultActionWhenCastFails()) return true;
		return false;
	}
	
	public static boolean isCancelStateOk(PassiveSpell spell, boolean cancelled) {
		if (spell.ignoreCancelled() && cancelled) return false;
		if (spell.requireCancelledEvent() && !cancelled) return false;
		return true;
	}
	
	public abstract void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var);
	
	public void initialize() {
		// No op
	}
	
	public void turnOff() {
		// No op
	}
	
	@Override
	public boolean equals(Object other) {
		return this == other; // Don't want to make things equal unless they are the same object
	}
	
}
