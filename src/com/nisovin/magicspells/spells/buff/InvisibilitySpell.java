package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;

import com.nisovin.magicspells.util.TimeUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

/**
 * InvisibilitySpell<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *         <th>
 *             Default
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             prevent-pickups
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             true
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-spell-cast
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             false
 *         </td>
 *     </tr>
 * </table>
 */
public class InvisibilitySpell extends BuffSpell {

	private boolean preventPickups;
	
	private boolean cancelOnSpellCast;
	
	private HashMap<String,CostCharger> invisibles = new HashMap<>();
	
	public InvisibilitySpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		preventPickups = getConfigBoolean("prevent-pickups", true);
		cancelOnSpellCast = getConfigBoolean("cancel-on-spell-cast", false);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (cancelOnSpellCast) registerEvents(new SpellCastListener());
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		makeInvisible(player);
		invisibles.put(player.getName(), new CostCharger(player));
		return true;
	}
	
	@Override
	public boolean recastBuff(Player player, float power, String[] args) {
		makeInvisible(player);
		String playerName = player.getName();
		if (invisibles.containsKey(playerName)) invisibles.put(playerName, new CostCharger(player));
		return true;
	}
	
	private void makeInvisible(Player player) {
		// Make player invisible
		Util.forEachPlayerOnline(p -> p.hidePlayer(player));
		
		// Detarget monsters
		Creature creature;
		for (Entity e : player.getNearbyEntities(30, 30, 30)) {
			if (!(e instanceof Creature)) continue;
			
			creature = (Creature)e;
			LivingEntity target = creature.getTarget();
			if (target == null) continue;
			if (!target.equals(player)) continue;
			
			creature.setTarget(null);
		}
	}
	
	@EventHandler
	public void onPlayerItemPickup(PlayerPickupItemEvent event) {
		if (!preventPickups) return;
		if (!invisibles.containsKey(event.getPlayer().getName())) return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;
		Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		if (!invisibles.containsKey(target.getName())) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		for (String name : invisibles.keySet()) {
			Player p = PlayerNameUtils.getPlayerExact(name);
			if (p == null) continue;
			if (name.equals(player.getName())) continue;
			player.hidePlayer(p);
		}
		
		if (invisibles.containsKey(player.getName())) {
			Util.forEachPlayerOnline(p -> p.hidePlayer(player));
		}
	}
	
	@Override
	public void turnOffBuff(Player player) {
		// Stop charge ticker
		CostCharger c = invisibles.remove(player.getName());
		if (c == null) return;
		c.stop();
		
		// Force visible
		Util.forEachPlayerOnline(p -> p.showPlayer(player));
	}

	@Override
	protected void turnOff() {
		Util.forEachValueOrdered(invisibles, CostCharger::stop);
		invisibles.clear();
	}
	
	public class SpellCastListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onSpellCast(SpellCastEvent event) {
			Player caster = event.getCaster();
			if (!isActive(caster)) return;
			if (event.getSpell().getInternalName().equals(internalName)) return;
			turnOff(caster);
		}
		
	}
	
	private class CostCharger implements Runnable {
		
		int taskId = -1;
		Player player;
		
		public CostCharger(Player player) {
			this.player = player;
			if (useCostInterval > 0) taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, TimeUtil.TICKS_PER_SECOND, TimeUtil.TICKS_PER_SECOND);
		}
		
		@Override
		public void run() {
			addUseAndChargeCost(player);
		}
		
		public void stop() {
			if (taskId == -1) return;
			Bukkit.getScheduler().cancelTask(taskId);
		}
		
	}

	@Override
	public boolean isActive(Player player) {
		return invisibles.containsKey(player.getName());
	}

}
