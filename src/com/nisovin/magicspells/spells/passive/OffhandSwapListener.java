package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

public class OffhandSwapListener extends PassiveListener {
	
	List<PassiveSpell> spells = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (spell != null) spells.add(spell);
	}
	
	@OverridePriority
	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		
		Spellbook spellbook = MagicSpells.getSpellbook(player);
		if (spellbook == null) return;
		
		for (PassiveSpell spell: spells) {
			if (!isCancelStateOk(spell, event.isCancelled())) continue;
			if (!spellbook.hasSpell(spell)) continue;
			boolean casted = spell.activate(player);
			if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
			event.setCancelled(true);
		}
	}

}
