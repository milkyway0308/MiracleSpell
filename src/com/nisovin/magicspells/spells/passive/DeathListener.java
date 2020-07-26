package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable used here
public class DeathListener extends PassiveListener {

	List<PassiveSpell> spells = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		spells.add(spell);
	}
	
	@OverridePriority
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Spellbook spellbook = MagicSpells.getSpellbook(player);
		spells.stream().filter(spellbook::hasSpell).forEachOrdered(spell -> spell.activate(player));
	}

}
