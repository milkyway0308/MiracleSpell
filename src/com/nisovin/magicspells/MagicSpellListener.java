package com.nisovin.magicspells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.events.SpellTargetEvent;

class MagicSpellListener implements Listener {
		
	public MagicSpellListener(MagicSpells plugin) {
		// No op
	}

	@EventHandler
	public void onSpellTarget(SpellTargetEvent event) {
		// Check if target has notarget permission
		LivingEntity target = event.getTarget();
		if (!(target instanceof Player)) return;
		if (Perm.NOTARGET.has(target)) event.setCancelled(true);
	}
	
}
