package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicItemWithNameMaterial;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Trigger variable is the item to trigger on
public class HotBarListener extends PassiveListener {

	Set<Material> materials = new HashSet<>();
	Map<MagicMaterial, List<PassiveSpell>> select = new LinkedHashMap<>();
	Map<MagicMaterial, List<PassiveSpell>> deselect = new LinkedHashMap<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		MagicMaterial mat;
		if (var.contains("|")) {
			String[] stuff = var.split("\\|");
			mat = MagicSpells.getItemNameResolver().resolveItem(stuff[0]);
			if (mat != null) mat = new MagicItemWithNameMaterial(mat, stuff[1]);						
		} else {
			mat = MagicSpells.getItemNameResolver().resolveItem(var);
		}
		if (mat != null) {
			materials.add(mat.getMaterial());
			List<PassiveSpell> list = null;
			if (PassiveTrigger.HOT_BAR_SELECT.contains(trigger)) {
				list = select.computeIfAbsent(mat, material -> new ArrayList<>());
			} else if (PassiveTrigger.HOT_BAR_DESELECT.contains(trigger)) {
				list = deselect.computeIfAbsent(mat, material -> new ArrayList<>());
			}
			if (list != null) list.add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onPlayerScroll(PlayerItemHeldEvent event) {
		if (!deselect.isEmpty()) {
			ItemStack item = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
			if (item != null && item.getType() != Material.AIR) {
				List<PassiveSpell> list = getSpells(item, deselect);
				if (list != null) {
					Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
					for (PassiveSpell spell : list) {
						if (!isCancelStateOk(spell, event.isCancelled())) continue;
						if (!spellbook.hasSpell(spell, false)) continue;
						boolean casted = spell.activate(event.getPlayer());
						if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
						event.setCancelled(true);
					}
				}
			}
		}
		if (!select.isEmpty()) {
			ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
			if (item != null && item.getType() != Material.AIR) {
				List<PassiveSpell> list = getSpells(item, select);
				if (list != null) {
					Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
					for (PassiveSpell spell : list) {
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
	
	private List<PassiveSpell> getSpells(ItemStack item, Map<MagicMaterial, List<PassiveSpell>> map) {
		if (!materials.contains(item.getType())) return null;
		for (Entry<MagicMaterial, List<PassiveSpell>> entry : map.entrySet()) {
			if (entry.getKey().equals(item)) return entry.getValue();
		}
		return null;
	}

}
