package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is used here
public class SprintListener extends PassiveListener {

	List<PassiveSpell> sprint = null;
	List<PassiveSpell> stopSprint = null;
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (PassiveTrigger.SPRINT.contains(trigger)) {
			if (sprint == null) sprint = new ArrayList<>();
			sprint.add(spell);
		} else if (PassiveTrigger.STOP_SPRINT.contains(trigger)) {
			if (sprint == null) stopSprint = new ArrayList<>();
			stopSprint.add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onSprint(PlayerToggleSprintEvent event) {
		if (event.isSprinting()) {
			if (sprint != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : sprint) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (spellbook.hasSpell(spell, false)) {
						boolean casted = spell.activate(event.getPlayer());
						if (PassiveListener.cancelDefaultAction(spell, casted)) {
							event.setCancelled(true);
						}
					}
				}
			}
		} else {
			if (stopSprint != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : stopSprint) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (spellbook.hasSpell(spell, false)) {
						boolean casted = spell.activate(event.getPlayer());
						if (PassiveListener.cancelDefaultAction(spell, casted)) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

}
