package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.nisovin.magicspells.Spell.SpellCastResult;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.util.CastItem;

public class ConsumeListener implements Listener {

	MagicSpells plugin;
	
	Map<CastItem, Spell> consumeCastItems = new HashMap<>();
	HashMap<String, Long> lastCast = new HashMap<>();
	
	public ConsumeListener(MagicSpells plugin) {
		this.plugin = plugin;
		for (Spell spell : plugin.spells.values()) {
			CastItem[] items = spell.getConsumeCastItems();
			if (items.length <= 0) continue;
			for (CastItem item : items) {
				Spell old = consumeCastItems.put(item, spell);
				if (old == null) continue;
				MagicSpells.error("The spell '" + spell.getInternalName() + "' has same consume-cast-item as '" + old.getInternalName() + "'!");
			}
		}
	}
	
	public boolean hasConsumeCastItems() {
		return !consumeCastItems.isEmpty();
	}
	
	@EventHandler
	public void onConsume(final PlayerItemConsumeEvent event) {
	    CastItem castItem = new CastItem(event.getItem());
	    final Spell spell = this.consumeCastItems.get(castItem);
	    if (spell == null) return;

	    Player player = event.getPlayer();
		Long lastCastTime = lastCast.get(player.getName());
		if (lastCastTime != null && lastCastTime + plugin.globalCooldown > System.currentTimeMillis()) return;
		lastCast.put(player.getName(), System.currentTimeMillis());
	    
	    if (MagicSpells.getSpellbook(player).canCast(spell)) {
	    	SpellCastResult result = spell.cast(event.getPlayer());
	    	if (result.state != SpellCastState.NORMAL) event.setCancelled(true);
	    }
	}
	
}
