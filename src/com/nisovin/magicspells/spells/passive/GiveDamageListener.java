package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicItemWithNameMaterial;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.OverridePriority;

// Optional trigger variable that may contain a comma separated list
// Of weapons to trigger on
public class GiveDamageListener extends PassiveListener {

	Set<Material> types = new HashSet<>();
	Map<MagicMaterial, List<PassiveSpell>> weapons = new LinkedHashMap<>();
	List<PassiveSpell> always = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			always.add(spell);
		} else {
			String[] split = var.split(",");
			for (String s : split) {
				s = s.trim();
				MagicMaterial mat;
				if (s.contains("|")) {
					String[] stuff = s.split("\\|");
					mat = MagicSpells.getItemNameResolver().resolveItem(stuff[0]);
					if (mat != null) mat = new MagicItemWithNameMaterial(mat, stuff[1]);						
				} else {
					mat = MagicSpells.getItemNameResolver().resolveItem(s);
				}
				if (mat != null) {
					List<PassiveSpell> list = weapons.computeIfAbsent(mat, magicMaterial -> new ArrayList<>());
					list.add(spell);
					types.add(mat.getMaterial());
				}
			}
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Player player = getPlayerAttacker(event);
		if (player == null || !(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity attacked = (LivingEntity)event.getEntity();
		Spellbook spellbook = null;
		
		if (!always.isEmpty()) {
			spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : always) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell, false)) continue;
				boolean casted = spell.activate(player, attacked);
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		}
		
		if (!weapons.isEmpty()) {
			ItemStack item = HandHandler.getItemInMainHand(player);
			if (item != null && item.getType() != Material.AIR) {
				List<PassiveSpell> list = getSpells(item);
				if (list != null) {
					if (spellbook == null) spellbook = MagicSpells.getSpellbook(player);
					for (PassiveSpell spell : list) {
						if (!isCancelStateOk(spell, event.isCancelled())) continue;
						if (!spellbook.hasSpell(spell, false)) continue;
						boolean casted = spell.activate(player, attacked);
						if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	private Player getPlayerAttacker(EntityDamageByEntityEvent event) {
		Entity e = event.getDamager();
		if (e instanceof Player) return (Player)e;
		if (e instanceof Projectile && ((Projectile)e).getShooter() instanceof Player) {
			return (Player)((Projectile)e).getShooter();
		}
		return null;
	}
	
	private List<PassiveSpell> getSpells(ItemStack item) {
		if (!types.contains(item.getType())) return null;
		for (Entry<MagicMaterial, List<PassiveSpell>> entry : weapons.entrySet()) {
			if (entry.getKey().equals(item)) return entry.getValue();
		}
		return null;
	}

}
