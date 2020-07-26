package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.MaterialData;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Optional trigger variable of a comma separated list of blocks to accept
public class BlockBreakListener extends PassiveListener {

	Set<Material> materials = new HashSet<>();
	Map<MagicMaterial, List<PassiveSpell>> types = new HashMap<>();
	List<PassiveSpell> allTypes = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			allTypes.add(spell);
		} else {
			String[] split = var.split(",");
			for (String s : split) {
				s = s.trim();
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
				if (m == null) continue;
				List<PassiveSpell> list = types.computeIfAbsent(m, material -> new ArrayList<>());
				list.add(spell);
				materials.add(m.getMaterial());
			}
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
		if (!allTypes.isEmpty()) {
			for (PassiveSpell spell : allTypes) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell, false)) continue;
				boolean casted = spell.activate(event.getPlayer(), event.getBlock().getLocation().add(0.5, 0.5, 0.5));
				if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
			}
		}
		if (!types.isEmpty()) {
			List<PassiveSpell> list = getSpells(event.getBlock());
			if (list != null) {
				for (PassiveSpell spell : list) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(event.getPlayer(), event.getBlock().getLocation().add(0.5, 0.5, 0.5));
					if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
				}
			}
		}
	}
	
	private List<PassiveSpell> getSpells(Block block) {
		if (!materials.contains(block.getType())) return null;
		MaterialData data = block.getState().getData();
		for (Entry<MagicMaterial, List<PassiveSpell>> entry : types.entrySet()) {
			if (entry.getKey().equals(data)) return entry.getValue();
		}
		return null;
	}

}
