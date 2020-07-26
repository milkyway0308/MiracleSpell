package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class RitualSpell extends InstantSpell {
	
	int ritualDuration;
	int reqParticipants;
	private boolean needSpellToParticipate;
	boolean showProgressOnExpBar;
	boolean chargeReagentsImmediately;
	boolean setCooldownImmediately;
	boolean setCooldownForAll;
	private Spell spell;
	private String theSpellName;
	int tickInterval;
	int effectInterval;
	private String strRitualJoined;
	String strRitualSuccess;
	String strRitualInterrupted;
	String strRitualFailed;
	
	HashMap<Player, ActiveRitual> activeRituals;
	
	public RitualSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.ritualDuration = getConfigInt("ritual-duration", 200);
		this.reqParticipants = getConfigInt("req-participants", 3);
		this.needSpellToParticipate = getConfigBoolean("need-spell-to-participate", false);
		this.showProgressOnExpBar = getConfigBoolean("show-progress-on-exp-bar", true);
		this.chargeReagentsImmediately = getConfigBoolean("charge-reagents-immediately", true);
		this.setCooldownImmediately = getConfigBoolean("set-cooldown-immediately", true);
		this.setCooldownForAll = getConfigBoolean("set-cooldown-for-all", true);
		this.theSpellName = getConfigString("spell", "");
		this.tickInterval = getConfigInt("tick-interval", 5);
		this.effectInterval = getConfigInt("effect-interval", TimeUtil.TICKS_PER_SECOND);
		this.strRitualJoined = getConfigString("str-ritual-joined", null);
		this.strRitualSuccess = getConfigString("str-ritual-success", null);
		this.strRitualInterrupted = getConfigString("str-ritual-interrupted", null);
		this.strRitualFailed = getConfigString("str-ritual-failed", null);
		
		this.activeRituals = new HashMap<>();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		this.spell = MagicSpells.getSpellByInternalName(this.theSpellName);
		if (this.spell == null) MagicSpells.error("RitualSpell '" + this.internalName + "' does not have a spell defined (" + this.theSpellName + ")!");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (this.activeRituals.containsKey(player)) {
			ActiveRitual channel = this.activeRituals.remove(player);
			channel.stop(this.strRitualInterrupted);
		}
		if (state == SpellCastState.NORMAL) {
			this.activeRituals.put(player, new ActiveRitual(player, power, args));
			if (!this.chargeReagentsImmediately && !this.setCooldownImmediately) return PostCastAction.MESSAGES_ONLY;
			if (!this.chargeReagentsImmediately) return PostCastAction.NO_REAGENTS;
			if (!this.setCooldownImmediately) return PostCastAction.NO_COOLDOWN;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			ActiveRitual channel = this.activeRituals.get(event.getRightClicked());
			if (channel == null) return;
			if (!this.needSpellToParticipate || hasThisSpell(event.getPlayer())) {
				channel.addChanneler(event.getPlayer());
				sendMessage(this.strRitualJoined, event.getPlayer(), MagicSpells.NULL_ARGS);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		for (ActiveRitual ritual : this.activeRituals.values()) {
			if (!ritual.isChanneler(event.getPlayer())) continue;
			ritual.stop(this.strInterrupted);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		for (ActiveRitual ritual : this.activeRituals.values()) {
			if (!ritual.isChanneler(event.getEntity())) continue;
			ritual.stop(this.strInterrupted);
		}
	}
	
	private boolean hasThisSpell(Player player) {
		return MagicSpells.getSpellbook(player).hasSpell(this);
	}
	
	public class ActiveRitual implements Runnable {
		
		private Player caster;
		private float power;
		private String[] args;
		private int duration = 0;
		private int taskId;
		private HashMap<Player, Location> channelers = new HashMap<>();
		
		public ActiveRitual(Player caster, float power, String[] args) {
			this.power = power;
			this.args = args;
			this.caster = caster;
			this.channelers.put(caster, caster.getLocation());
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, tickInterval, tickInterval);
			if (showProgressOnExpBar) MagicSpells.getExpBarManager().lock(caster, this);
			playSpellEffects(EffectPosition.CASTER, caster);
		}
		
		public void addChanneler(Player player) {
			if (this.channelers.containsKey(player)) return;
			this.channelers.put(player, player.getLocation());
			if (showProgressOnExpBar) MagicSpells.getExpBarManager().lock(player, this);
			playSpellEffects(EffectPosition.CASTER, player);
		}
		
		public void removeChanneler(Player player) {
			channelers.remove(player);
		}
		
		public boolean isChanneler(Player player) {
			return channelers.containsKey(player);
		}
		
		@Override
		public void run() {
			this.duration += tickInterval;

			int count = this.channelers.size();
			boolean interrupted = false;
			Iterator<Map.Entry<Player, Location>> iter = this.channelers.entrySet().iterator();
			while (iter.hasNext()) {
				Player player = iter.next().getKey();
				
				// Check for movement/death/offline
				Location oldloc = this.channelers.get(player);
				Location newloc = player.getLocation();
				if (!player.isOnline() || player.isDead() || Math.abs(oldloc.getX() - newloc.getX()) > .2 || Math.abs(oldloc.getY() - newloc.getY()) > .2 || Math.abs(oldloc.getZ() - newloc.getZ()) > .2) {
					if (player.getName().equals(this.caster.getName())) {
						interrupted = true;
						break;
					} else {
						iter.remove();
						count--;
						resetManaBar(player);
						continue;
					}
				}
				// Send exp bar update
				if (showProgressOnExpBar) {
					MagicSpells.getExpBarManager().update(player, count, (float)this.duration / (float)ritualDuration, this);
				}
				// Spell effect
				if (this.duration % effectInterval == 0) {
					playSpellEffects(EffectPosition.CASTER, player);
				}
			}
			if (interrupted) {
				stop(strRitualInterrupted);
				if (spellOnInterrupt != null && this.caster.isValid()) {
					spellOnInterrupt.castSpell(this.caster, SpellCastState.NORMAL, this.power, MagicSpells.NULL_ARGS);
				}
			}
			
			if (this.duration >= ritualDuration) {
				// Channel is done
				if (count >= reqParticipants && !this.caster.isDead() && this.caster.isOnline()) {
					if (chargeReagentsImmediately || hasReagents(this.caster)) {
						stop(strRitualSuccess);
						playSpellEffects(EffectPosition.DELAYED, this.caster);
						PostCastAction action = spell.castSpell(this.caster, SpellCastState.NORMAL, this.power, args);
						if (!chargeReagentsImmediately && action.chargeReagents()) {
							removeReagents(this.caster);
						}
						if (!setCooldownImmediately && action.setCooldown()) {
							setCooldown(this.caster, cooldown);
						}
						if (setCooldownForAll && action.setCooldown()) {
							for (Player p : this.channelers.keySet()) {
								setCooldown(p, cooldown);
							}
						}
					} else {
						stop(strRitualFailed);
					}
				} else {
					stop(strRitualFailed);
				}
			}
		}
		
		public void stop(String message) {
			for (Player player : this.channelers.keySet()) {
				sendMessage(message, player, MagicSpells.NULL_ARGS);
				resetManaBar(player);
			}
			this.channelers.clear();
			Bukkit.getScheduler().cancelTask(this.taskId);
			activeRituals.remove(this.caster);
		}
		
		private void resetManaBar(Player player) {
			MagicSpells.getExpBarManager().unlock(player, this);
			MagicSpells.getExpBarManager().update(player, player.getLevel(), player.getExp());
			if (MagicSpells.getManaHandler() != null) {
				MagicSpells.getManaHandler().showMana(player);
			}
			
		}
		
	}

}
