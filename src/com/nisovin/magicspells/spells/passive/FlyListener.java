package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is currently used
public class FlyListener extends PassiveListener {

	List<PassiveSpell> fly = null;
	List<PassiveSpell> stopFly = null;
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (PassiveTrigger.FLY.contains(trigger)) {
			if (fly == null) fly = new ArrayList<>();
			fly.add(spell);
		} else if (PassiveTrigger.STOP_FLY.contains(trigger)) {
			if (stopFly == null) stopFly = new ArrayList<>();
			stopFly.add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onFly(PlayerToggleFlightEvent event) {
		if (event.isFlying()) {
			if (fly != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : fly) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(event.getPlayer());
					if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
					event.setCancelled(true);
				}
			}
		} else {
			if (stopFly != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				for (PassiveSpell spell : stopFly) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(event.getPlayer());
					if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
					event.setCancelled(true);
				}
			}
		}
	}

}
