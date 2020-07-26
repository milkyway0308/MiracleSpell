package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.util.Util;

// Trigger variable is optional
// If not specified, it triggers in all forms
// The trigger variable may be a comma separated list containing any of the following
// ground, fish, fail, <entity type>
public class FishListener extends PassiveListener {

	Map<EntityType, List<PassiveSpell>> types = new HashMap<>();
	List<PassiveSpell> ground = new ArrayList<>();
	List<PassiveSpell> fish = new ArrayList<>();
	List<PassiveSpell> fail = new ArrayList<>();
	List<PassiveSpell> allTypes = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			allTypes.add(spell);
		} else {
			String[] split = var.replace(" ", "").toUpperCase().split(",");
			for (String s : split) {
				if (s.equalsIgnoreCase("ground")) {
					ground.add(spell);
				} else if (s.equalsIgnoreCase("fish")) {
					fish.add(spell);
				} else if (s.equalsIgnoreCase("fail")) {
					fail.add(spell);
				} else {
					EntityType t = Util.getEntityType(s);
					if (t != null) {
						List<PassiveSpell> list = types.computeIfAbsent(t, type -> new ArrayList<>());
						list.add(spell);
					}
				}
			}
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onFish(PlayerFishEvent event) {
		PlayerFishEvent.State state = event.getState();
		Player player = event.getPlayer();
		
		if (!allTypes.isEmpty()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			Entity entity = event.getCaught();
			for (PassiveSpell spell : allTypes) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player, entity instanceof LivingEntity ? (LivingEntity)entity : null);
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		}
		
		if (state == State.IN_GROUND && !ground.isEmpty()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : ground) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player, event.getHook().getLocation());
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		} else if (state == State.CAUGHT_FISH && !fish.isEmpty()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : fish) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player, event.getHook().getLocation());
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		} else if (state == State.FAILED_ATTEMPT && !fail.isEmpty()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : fail) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player, event.getHook().getLocation());
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		} else if (state == State.CAUGHT_ENTITY && !types.isEmpty()) {
			Entity entity = event.getCaught();
			if (entity != null && types.containsKey(entity.getType())) {
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				for (PassiveSpell spell : fail) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell)) continue;
					boolean casted = spell.activate(player, entity instanceof LivingEntity ? (LivingEntity)entity : null);
					if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
					event.setCancelled(true);
				}
			}
		}
	}
	
}
