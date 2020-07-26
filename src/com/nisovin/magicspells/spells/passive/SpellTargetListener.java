package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Optional trigger variable of comma separated list of internal spell names to accept
public class SpellTargetListener extends PassiveListener {

	Map<Spell, List<PassiveSpell>> spells = new HashMap<>();
	List<PassiveSpell> anySpell = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			anySpell.add(spell);
		} else {
			String[] split = var.split(",");
			for (String s : split) {
				Spell sp = MagicSpells.getSpellByInternalName(s.trim());
				if (sp == null) continue;
				List<PassiveSpell> passives = spells.computeIfAbsent(sp, p -> new ArrayList<>());
				passives.add(spell);
			}
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onSpellTarget(SpellTargetEvent event) {
		Spellbook spellbook = MagicSpells.getSpellbook(event.getCaster());
		for (PassiveSpell spell : anySpell) {
			if (!isCancelStateOk(spell, event.isCancelled())) continue;
			if (!spellbook.hasSpell(spell, false)) continue;
			boolean casted = spell.activate(event.getCaster(), event.getTarget());
			if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
		}
		List<PassiveSpell> list = spells.get(event.getSpell());
		if (list != null) {
			for (PassiveSpell spell : list) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell, false)) continue;
				boolean casted = spell.activate(event.getCaster(), event.getTarget());
				if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
			}
		}
	}

}
