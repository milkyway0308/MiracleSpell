package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// No trigger variable is used here
public class RespawnListener extends PassiveListener {

	List<PassiveSpell> spells = new ArrayList<>();

	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		spells.add(spell);
	}
	
	@OverridePriority
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		if (spells.isEmpty()) return;
		final Player player = event.getPlayer();
		final Spellbook spellbook = MagicSpells.getSpellbook(player);
		MagicSpells.scheduleDelayedTask(() -> spells.stream().filter(spellbook::hasSpell).forEachOrdered(spell -> spell.activate(player)), 1);
	}

}
