package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is used here
public class LeaveBedListener extends PassiveListener {

	List<PassiveSpell> spells = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		spells.add(spell);
	}
	
	@OverridePriority
	@EventHandler
	public void onDeath(PlayerBedLeaveEvent event) {
		Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
		spells.stream().filter(spellbook::hasSpell).forEachOrdered(spell -> spell.activate(event.getPlayer()));
	}

}
