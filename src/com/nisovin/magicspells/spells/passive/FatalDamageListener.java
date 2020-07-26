package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is used here
public class FatalDamageListener extends PassiveListener {

	List<PassiveSpell> spells = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		spells.add(spell);
	}

	@OverridePriority
	@EventHandler
	void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		if (event.getFinalDamage() >= player.getHealth()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : spells) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player);
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		}
	}
	
}
