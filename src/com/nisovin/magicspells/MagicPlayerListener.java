package com.nisovin.magicspells;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class MagicPlayerListener implements Listener {
	
	private MagicSpells plugin;
	
	public MagicPlayerListener(MagicSpells plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		// set up spell book
		Spellbook spellbook = new Spellbook(player, plugin);
		plugin.spellbooks.put(player.getName(), spellbook);
		
		// set up mana bar
		if (plugin.mana != null) {
			plugin.mana.createManaBar(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Spellbook spellbook = plugin.spellbooks.remove(event.getPlayer().getName());
		if (spellbook != null) {
			spellbook.destroy();
		}
	}
	
	// DEBUG INFO: level 2, player changed from world to world, reloading spells
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		if (plugin.separatePlayerSpellsPerWorld) {
			Player player = event.getPlayer();
			MagicSpells.debug(2, "Player '" + player.getName() + "' changed from world '" + event.getFrom().getName() + "' to '" + player.getWorld().getName() + "', reloading spells");
			MagicSpells.getSpellbook(player).reload();
		}
	}
	
}
