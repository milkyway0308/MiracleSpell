package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Optional trigger variable that can either be set to a dye color to accept or "all"
public class SheepShearListener extends PassiveListener {

	EnumMap<DyeColor, List<PassiveSpell>> spellMap = new EnumMap<>(DyeColor.class);
	List<PassiveSpell> allColorSpells = new ArrayList<>();
	
	List<PassiveSpell> spellsLoaded = new ArrayList<>();
	List<PassiveSpell> spellsDeclined = new ArrayList<>();
	List<PassiveSpell> spellsFailed = new ArrayList<>();
	List<PassiveSpell> spellsAccepted = new ArrayList<>();
	
	@Override
	public void initialize() {
		super.initialize();
		for (DyeColor c: DyeColor.values()) {
			spellMap.put(c, new ArrayList<>());
		}
	}
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.equalsIgnoreCase("all")) {
			allColorSpells.add(spell);
		} else {
			DyeColor c = DyeColor.valueOf(var.toUpperCase());
			if (c == null) throw new IllegalArgumentException("Cannot resolve " + var + " to DyeColor");
			spellMap.get(DyeColor.valueOf(var.toUpperCase())).add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onSheepShear(PlayerShearEntityEvent event) {
		if (!(event.getEntity() instanceof Sheep)) return;
		Sheep s = (Sheep)event.getEntity();
		Player p = event.getPlayer();
		List<PassiveSpell> spells = spellMap.get(s.getColor());
		Spellbook spellbook = MagicSpells.getSpellbook(p);
		for (PassiveSpell spell : spells) {
			if (!isCancelStateOk(spell, event.isCancelled())) continue;
			if (!spellbook.hasSpell(spell)) continue;
			boolean casted = spell.activate(p);
			if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
		}
		for (PassiveSpell spell: allColorSpells) {
			if (!isCancelStateOk(spell, event.isCancelled())) continue;
			if (!spellbook.hasSpell(spell)) continue;
			boolean casted = spell.activate(p);
			if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
		}
	}

}
