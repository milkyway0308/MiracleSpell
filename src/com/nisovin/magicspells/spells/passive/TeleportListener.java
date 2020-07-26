package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Optional trigger variable of comma separated list of teleport causes to accept
public class TeleportListener extends PassiveListener {

	Map<TeleportCause, List<PassiveSpell>> types = new HashMap<>();
	List<PassiveSpell> allTypes = new ArrayList<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			allTypes.add(spell);
		} else {
			String[] split = var.replace(" ", "").split(",");
			for (String s : split) {
				s = s.trim().replace("_", "");
				for (TeleportCause cause : TeleportCause.values()) {
					if (cause.name().replace("_", "").equalsIgnoreCase(s)) {
						List<PassiveSpell> list = types.computeIfAbsent(cause, c -> new ArrayList<>());
						list.add(spell);
						break;
					}
				}
			}
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		
		if (!allTypes.isEmpty()) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : allTypes) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (spellbook.hasSpell(spell)) {
					boolean casted = spell.activate(player);
					if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
				}
			}
		}
		
		if (!types.isEmpty() && types.containsKey(event.getCause())) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (PassiveSpell spell : types.get(event.getCause())) {
				if (!isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell)) continue;
				boolean casted = spell.activate(player);
				if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
			}
		}
	}

}
