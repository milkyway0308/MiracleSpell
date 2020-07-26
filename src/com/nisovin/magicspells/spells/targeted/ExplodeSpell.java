package com.nisovin.magicspells.spells.targeted;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;

public class ExplodeSpell extends TargetedSpell implements TargetedLocationSpell {
	
	private int explosionSize;
	private int backfireChance;
	private boolean simulateTnt;
	private boolean preventBlockDamage;
	private boolean preventPlayerDamage;
	private boolean preventAnimalDamage;
	private float damageMultiplier;
	private boolean addFire;
	private boolean ignoreCanceled;
	
	private long currentTick = 0;
	private float currentPower = 0;
	
	public ExplodeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		explosionSize = getConfigInt("explosion-size", 4);
		backfireChance = getConfigInt("backfire-chance", 0);
		simulateTnt = getConfigBoolean("simulate-tnt", true);
		preventBlockDamage = getConfigBoolean("prevent-block-damage", false);
		preventPlayerDamage = getConfigBoolean("prevent-player-damage", false);
		preventAnimalDamage = getConfigBoolean("prevent-animal-damage", false);
		damageMultiplier = getConfigFloat("damage-multiplier", 0);
		addFire = getConfigBoolean("add-fire", false);
		ignoreCanceled = getConfigBoolean("ignore-canceled", false);
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = null;
			try {
				target = getTargetedBlock(player, power);
			} catch (IllegalStateException e) {
				DebugHandler.debugIllegalState(e);
				target = null;
			}
			if (target != null && target.getType() != Material.AIR) {
				SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, player, target.getLocation(), power);
				EventUtil.call(event);
				if (event.isCancelled()) {
					target = null;
				} else {
					target = event.getTargetLocation().getBlock();
					power = event.getPower();
				}
			}
			if (target == null || target.getType() == Material.AIR) {
				// Fail: no target
				return noTarget(player);
			}
			boolean exploded = explode(player, target.getLocation(), power);
			if (!exploded && !ignoreCanceled) return noTarget(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean explode(Player player, Location target, float power) {
		// Check plugins
		if (simulateTnt) {
			boolean cancelled = MagicSpells.getVolatileCodeHandler().simulateTnt(target, player, explosionSize * power, addFire);
			if (cancelled) return false;
		}
		// Backfire chance
		if (backfireChance > 0) {
			Random rand = new Random();
			if (rand.nextInt(10000) < backfireChance) target = player.getLocation();
		}
		// Save current explosion data
		currentTick = Bukkit.getWorlds().get(0).getFullTime();
		currentPower = power;
		// Cause explosion
		boolean ret = MagicSpells.getVolatileCodeHandler().createExplosionByPlayer(player, target, explosionSize * power, addFire, !preventBlockDamage);
		if (ret) playSpellEffects(player, target);
		return ret;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return explode(caster, target, power);
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		return false;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		if (!(damageMultiplier > 0 || preventPlayerDamage)) return;
		if (!(event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION)) return;
		if (currentTick != Bukkit.getWorlds().get(0).getFullTime()) return;
		if (preventPlayerDamage && event.getEntity() instanceof Player) {
			event.setCancelled(true);
		} else if (preventAnimalDamage && event.getEntity() instanceof Animals) {
			event.setCancelled(true);
		} else if (damageMultiplier > 0) {
			event.setDamage(Math.round(event.getDamage() * damageMultiplier * currentPower));
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		if (event.isCancelled() || !preventBlockDamage) return;
		
		if (currentTick == Bukkit.getWorlds().get(0).getFullTime()) {
			event.blockList().clear();
			event.setYield(0);
		}
	}
	
}
