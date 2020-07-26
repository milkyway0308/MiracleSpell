package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellForgetEvent;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;

// Trigger argument is required
// Must be an integer.
// The value reflects how often the trigger runs
// Where the value of the trigger variable is x
// The trigger will activate every x ticks
public class TicksListener extends PassiveListener {

	Map<Integer, Ticker> tickers = new HashMap<>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		try {
			int interval = Integer.parseInt(var);
			Ticker ticker = tickers.computeIfAbsent(interval, Ticker::new);
			ticker.add(spell);
		} catch (NumberFormatException e) {
			// No op
		}
	}
	
	@Override
	public void initialize() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.isValid()) continue;
			for (Ticker ticker : tickers.values()) {
				ticker.add(player);
			}
		}
	}
	
	@Override
	public void turnOff() {
		for (Ticker ticker : tickers.values()) {
			ticker.turnOff();
		}
		tickers.clear();
	}
	
	@OverridePriority
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		for (Ticker ticker : tickers.values()) {
			ticker.add(player);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		for (Ticker ticker : tickers.values()) {
			ticker.remove(player);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		for (Ticker ticker : tickers.values()) {
			ticker.remove(player);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		for (Ticker ticker : tickers.values()) {
			ticker.add(player);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onLearn(SpellLearnEvent event) {
		Spell spell = event.getSpell();
		if (!(spell instanceof PassiveSpell)) return;
		for (Ticker ticker : tickers.values()) {
			if (!ticker.monitoringSpell((PassiveSpell)spell)) continue;
			ticker.add(event.getLearner(), (PassiveSpell)spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onForget(SpellForgetEvent event) {
		Spell spell = event.getSpell();
		if (!(spell instanceof PassiveSpell)) return;
		for (Ticker ticker : tickers.values()) {
			if (!ticker.monitoringSpell((PassiveSpell)spell)) continue;
			ticker.remove(event.getForgetter(), (PassiveSpell)spell);
		}
	}
	
	class Ticker implements Runnable {

		int taskId;
		Map<PassiveSpell, Collection<Player>> spells = new HashMap<>();
		String profilingKey;
		
		public Ticker(int interval) {
			taskId = MagicSpells.scheduleRepeatingTask(this, interval, interval);
			profilingKey = MagicSpells.profilingEnabled() ? "PassiveTick:" + interval : null;
		}
		
		public void add(PassiveSpell spell) {
			spells.put(spell, new HashSet<>());
		}
		
		public void add(Player player) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			for (Entry<PassiveSpell, Collection<Player>> entry : spells.entrySet()) {
				if (spellbook.hasSpell(entry.getKey())) entry.getValue().add(player);
			}
		}
		
		public void add(Player player, PassiveSpell spell) {
			spells.get(spell).add(player);
		}
		
		public void remove(Player player) {
			for (Collection<Player> players : spells.values()) {
				players.remove(player);
			}
		}
		
		public void remove(Player player, PassiveSpell spell) {
			spells.get(spell).remove(player);
		}
		
		public boolean monitoringSpell(PassiveSpell spell) {
			return spells.containsKey(spell);
		}
		
		@Override
		public void run() {
			long start = System.nanoTime();
			for (Map.Entry<PassiveSpell, Collection<Player>> entry : spells.entrySet()) {
				Collection<Player> players = entry.getValue();
				if (players.isEmpty()) continue;
				for (Player p : new ArrayList<>(players)) {
					if (p.isOnline() && p.isValid()) {
						entry.getKey().activate(p);
					} else {
						players.remove(p);
					}
				}
			}
			if (profilingKey != null) MagicSpells.addProfile(profilingKey, System.nanoTime() - start);
		}
		
		public void turnOff() {
			MagicSpells.cancelTask(taskId);
		}
		
	}
	
}
