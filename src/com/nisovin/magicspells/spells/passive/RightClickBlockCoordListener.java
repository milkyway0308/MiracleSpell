package com.nisovin.magicspells.spells.passive;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicLocation;
import com.nisovin.magicspells.util.OverridePriority;

// Trigger variable is a semicolon separated list of locations to accept
// Locations follow the format of world,x,y,z
// Where "world" is a string and x, y, and z are integers
public class RightClickBlockCoordListener extends PassiveListener {

	Map<MagicLocation, PassiveSpell> locs = new HashMap<>();
	Map<MagicLocation, PassiveSpell> offhandLocs = new HashMap<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		Map<MagicLocation, PassiveSpell> addTo;
		if (isMainHandListener(trigger)) {
			addTo = locs;
		} else {
			addTo = offhandLocs;
		}
		String[] split = var.split(";");
		for (String s : split) {
			try {
				String[] data = s.split(",");
				String world = data[0];
				int x = Integer.parseInt(data[1]);
				int y = Integer.parseInt(data[2]);
				int z = Integer.parseInt(data[3]);				
				addTo.put(new MagicLocation(world, x, y, z), spell);
			} catch (NumberFormatException e) {
				MagicSpells.error("Invalid coords on rightclickblockcoord trigger for spell '" + spell.getInternalName() + '\'');
			}
		}
	}
	
	public boolean isMainHandListener(PassiveTrigger trigger) {
		return PassiveTrigger.RIGHT_CLICK_BLOCK_COORD.contains(trigger);
	}
	
	@OverridePriority
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Location location = event.getClickedBlock().getLocation();
		MagicLocation loc = new MagicLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		
		PassiveSpell spell;
		if (HandHandler.isMainHand(event)) {
			spell = locs.get(loc);
		} else {
			spell = offhandLocs.get(loc);
		}
		
		if (spell != null) {
			if (!isCancelStateOk(spell, event.isCancelled())) return;
			Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
			if (spellbook.hasSpell(spell, false)) {
				boolean casted = spell.activate(event.getPlayer(), location.add(0.5, 0.5, 0.5));
				if (PassiveListener.cancelDefaultAction(spell, casted)) event.setCancelled(true);
			}
		}
	}

}
