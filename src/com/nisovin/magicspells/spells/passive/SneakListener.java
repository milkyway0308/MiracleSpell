package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is currently used
public class SneakListener extends PassiveListener {

	List<PassiveSpell> sneak = null;
	List<PassiveSpell> stopSneak = null;
		
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (PassiveTrigger.SNEAK.contains(trigger)) {
			if (sneak == null) sneak = new ArrayList<>();
			sneak.add(spell);
		} else if (PassiveTrigger.STOP_SNEAK.contains(trigger)) {
			if (stopSneak == null) stopSneak = new ArrayList<>();
			stopSneak.add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isSneaking()) {
			if (sneak != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : sneak) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(event.getPlayer());
					if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
				}
			}
		} else {
			if (stopSneak != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : stopSneak) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(event.getPlayer());
					if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
				}
			}
		}
	}

}
