package com.nisovin.magicspells.spells;

import java.util.HashMap;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.nisovin.magicspells.BuffManager;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.LocationUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.util.SpellReagents;
import com.nisovin.magicspells.util.TargetInfo;

/**
 * BuffSpell<br>
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
 *     </tr>
 *     <tr>
 *         <td>
 *             targeted
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             toggle
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             use-cost
 *         </td>
 *         <td>
 *             Reagents
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             use-cost-interval
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             num-uses
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             duration
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             power-affects-duration
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-give-damage
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-take-damage
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-death
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-teleport
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-change-world
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
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
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cancel-on-logout
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             str-fade
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             can-cast-with-item
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             can-cast-by-command
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *            ???
 *         </td>
 *     </tr>
 * </table>
 */
public abstract class BuffSpell extends TargetedSpell implements TargetedEntitySpell {
	
	BuffSpell thisSpell;
	
	protected boolean targeted;
	
	protected boolean toggle;
	
	protected int healthCost = 0;
	protected int manaCost = 0;
	protected int hungerCost = 0;
	protected int experienceCost = 0;
	protected int levelsCost = 0;
	
	protected SpellReagents reagents;
	
	protected int useCostInterval;
	
	protected int numUses;
	
	protected float duration;
	
	protected boolean powerAffectsDuration;
	protected boolean cancelOnGiveDamage;
	protected boolean cancelOnTakeDamage;
	protected boolean cancelOnDeath;
	protected boolean cancelOnTeleport;
	protected boolean cancelOnChangeWorld;
	protected boolean cancelOnSpellCast;
	protected boolean cancelOnLogout;
	protected String strFade;
	private boolean castWithItem;
	private boolean castByCommand;
	
	private HashMap<String,Integer> useCounter;
	private HashMap<String,Long> durationEndTime;
	
	private String spellOnUseIncrementName = null;
	private Spell spellOnUseIncrement = null;
	
	private String spellOnCostName = null;
	private Spell spellOnCost = null;
	
	
	public BuffSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		this.thisSpell = this;
		
		this.targeted = getConfigBoolean("targeted", false);
		this.toggle = getConfigBoolean("toggle", true);
		this.reagents = getConfigReagents("use-cost");
		this.useCostInterval = getConfigInt("use-cost-interval", 0);
		this.numUses = getConfigInt("num-uses", 0);
		this.duration = getConfigFloat("duration", 0);
		this.powerAffectsDuration = getConfigBoolean("power-affects-duration", true);
		this.cancelOnGiveDamage = getConfigBoolean("cancel-on-give-damage", false);
		this.cancelOnTakeDamage = getConfigBoolean("cancel-on-take-damage", false);
		this.cancelOnDeath = getConfigBoolean("cancel-on-death", false);
		this.cancelOnTeleport = getConfigBoolean("cancel-on-teleport", false);
		this.cancelOnChangeWorld = getConfigBoolean("cancel-on-change-world", false);
		this.cancelOnSpellCast = getConfigBoolean("cancel-on-spell-cast", false);
		this.cancelOnLogout = getConfigBoolean("cancel-on-logout", false);
		this.spellOnUseIncrementName = getConfigString("spell-on-use-increment", null);
		this.spellOnCostName = getConfigString("spell-on-cost", null);
		if (this.cancelOnGiveDamage || this.cancelOnTakeDamage) registerEvents(new DamageListener());
		if (this.cancelOnDeath) registerEvents(new DeathListener());
		if (this.cancelOnTeleport) registerEvents(new TeleportListener());
		if (this.cancelOnChangeWorld) registerEvents(new ChangeWorldListener());
		if (this.cancelOnSpellCast) registerEvents(new SpellCastListener());
		if (this.cancelOnLogout) registerEvents(new QuitListener());
		
		this.strFade = getConfigString("str-fade", "");
		
		if (this.numUses > 0 || (this.reagents != null && this.useCostInterval > 0)) {
			this.useCounter = new HashMap<>();
		}
		if (this.duration > 0) this.durationEndTime = new HashMap<>();
		
		this.castWithItem = getConfigBoolean("can-cast-with-item", true);
		this.castByCommand = getConfigBoolean("can-cast-by-command", true);
	}
	
	@Override
	public void initialize() {
		// Super
		super.initialize();
		
		// Check spell on use increment
		if (this.spellOnUseIncrementName != null) {
			this.spellOnUseIncrement = MagicSpells.getSpellByInternalName(this.spellOnUseIncrementName);
			if (this.spellOnUseIncrement == null) {
				MagicSpells.error("Invalid spell-on-use-increment defined for " + this.internalName);
			}
		}
		
		// Check spell on cost
		if (this.spellOnCostName != null) {
			this.spellOnCost = MagicSpells.getSpellByInternalName(this.spellOnCostName);
			if (this.spellOnCost == null) {
				MagicSpells.error("Invalid spell-on-cost defined for " + this.internalName);
			}
		}
	}
	
	@Override
	public boolean canCastWithItem() {
		return this.castWithItem;
	}
	
	@Override
	public boolean canCastByCommand() {
		return this.castByCommand;
	}
	
	@Override
	public final PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		Player target;
		if (this.targeted) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
			if (targetInfo == null) return noTarget(player);
			target = targetInfo.getTarget();
			power = targetInfo.getPower();
		} else {
			target = player;
		}
		PostCastAction action = activate(player, target, power, args, state == SpellCastState.NORMAL);
		if (this.targeted && action == PostCastAction.HANDLE_NORMALLY) {
			sendMessages(player, target);
			return PostCastAction.NO_MESSAGES;
		}
		return action;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!(target instanceof Player)) return false;
		return activate(caster, (Player)target, power, MagicSpells.NULL_ARGS, true) == PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!(target instanceof Player)) return false;
		return activate(null, (Player)target, power, MagicSpells.NULL_ARGS, true) == PostCastAction.HANDLE_NORMALLY;
	}
	
	private PostCastAction activate(Player caster, Player target, float power, String[] args, boolean normal) {
		if (isActive(target)) {
			if (this.toggle) {
				turnOff(target);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (normal) {
				boolean ok = recastBuff(target, power, args);
				if (ok) {
					startSpellDuration(target, power);
					if (caster == null) {
						playSpellEffects(EffectPosition.TARGET, target);
					} else {
						playSpellEffects(caster, target);
					}
				}
			}
			return PostCastAction.HANDLE_NORMALLY;
		}
		if (normal) {
			boolean ok = castBuff(target, power, args);
			if (ok) {
				startSpellDuration(target, power);
				if (caster == null) {
					playSpellEffects(EffectPosition.TARGET, target);
				} else {
					playSpellEffects(caster, target);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public abstract boolean castBuff(Player player, float power, String[] args);
	
	public boolean recastBuff(Player player, float power, String[] args) {
		return true;
	}
	
	public void setAsEverlasting() {
		this.duration = 0;
		this.numUses = 0;
		this.useCostInterval = 0;
	}
	
	/**
	 * Begins counting the spell duration for a player
	 * @param player the player to begin counting duration
	 */
	private void startSpellDuration(final Player player, float power) {
		if (this.duration > 0 && this.durationEndTime != null) {
			float dur = this.duration;
			if (this.powerAffectsDuration) dur *= power;
			this.durationEndTime.put(player.getName(), System.currentTimeMillis() + Math.round(dur * TimeUtil.MILLISECONDS_PER_SECOND));
			final String name = player.getName();
			// TODO convert this to lambda
			Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
				@Override
				public void run() {
					Player p = PlayerNameUtils.getPlayerExact(name);
					if (p == null) p = player;
					if (isExpired(p)) turnOff(p);
				}
			}, Math.round(dur * TimeUtil.TICKS_PER_SECOND) + 20); // overestimate ticks, since the duration is real-time ms based
		}
		
		playSpellEffectsBuff(player, entity -> thisSpell.isActiveAndNotExpired((Player)entity));
		
		BuffManager buffman = MagicSpells.getBuffManager();
		if (buffman != null) buffman.addBuff(player, this);
	}
	
	/**
	 * Checks whether the spell's duration has expired for a player
	 * @param player the player to check
	 * @return true if the spell has expired, false otherwise
	 */
	protected boolean isExpired(Player player) {
		if (this.duration <= 0 || this.durationEndTime == null) return false;
		Long endTime = this.durationEndTime.get(player.getName());
		if (endTime == null) return false;
		if (endTime > System.currentTimeMillis()) return false;
		return true;
	}
	
	public boolean isActiveAndNotExpired(Player player) {
		if (this.duration > 0 && isExpired(player)) return false;
		return isActive(player);
	}
	
	/**
	 * Checks if this buff spell is active for the specified player
	 * @param player the player to check
	 * @return true if the spell is active, false otherwise
	 */
	public abstract boolean isActive(Player player);
	
	/**
	 * Adds a use to the spell for the player. If the number of uses exceeds the amount allowed, the spell will immediately expire.
	 * This does not automatically charge the use cost.
	 * @param player the player to add the use for
	 * @return the player's current number of uses (returns 0 if the use counting feature is disabled)
	 */
	protected int addUse(Player player) {
		// Run spell on use increment first thing in case we want to intervene
		if (this.spellOnUseIncrement != null) this.spellOnUseIncrement.cast(player, 1F, null);
		
		if (this.numUses > 0 || (this.reagents != null && this.useCostInterval > 0)) {
			String playerName = player.getName();
			Integer uses = this.useCounter.get(playerName);
			if (uses == null) {
				uses = 1;
			} else {
				uses++;
			}
			
			if (this.numUses > 0 && uses >= this.numUses) {
				turnOff(player);
			} else {
				this.useCounter.put(playerName, uses);
			}
			return uses;
		}
		return 0;
	}
	
	/**
	 * Removes this spell's use cost from the player's inventory. If the reagents aren't available, the spell will expire.
	 * @param player the player to remove the cost from
	 * @return true if the reagents were removed, or if the use cost is disabled, false otherwise
	 */
	protected boolean chargeUseCost(Player player) {
		// Run spell on cost first thing to dodge the early returns and allow intervention
		if (this.spellOnCost != null) this.spellOnCost.cast(player, 1F, null);
		
		if (this.reagents == null) return true;
		if (this.useCostInterval <= 0) return true;
		if (this.useCounter == null) return true;
		
		String playerName = player.getName();
		Integer uses = this.useCounter.get(playerName);
		if (uses == null) return true;
		
		if (uses % this.useCostInterval == 0) {
			if (hasReagents(player, this.reagents)) {
				removeReagents(player, this.reagents);
				return true;
			}
			turnOff(player);
			return false;
		}
		return true;
	}
	
	/**
	 * Adds a use to the spell for the player. If the number of uses exceeds the amount allowed, the spell will immediately expire.
	 * Removes this spell's use cost from the player's inventory. This does not return anything, to get useful return values, use
	 * addUse() and chargeUseCost().
	 * @param player the player to add a use and charge cost to
	 */
	protected void addUseAndChargeCost(Player player) {
		addUse(player);
		chargeUseCost(player);
	}
	
	/**
	 * Turns off this spell for the specified player. This can be called from many situations, including when the spell expires or the uses run out.
	 * When overriding this function, you should always be sure to call super.turnOff(player).
	 * @param player
	 */
	public final void turnOff(Player player) {
		if (!isActive(player)) return;
		String playerName = player.getName();
		if (this.useCounter != null) this.useCounter.remove(playerName);
		if (this.durationEndTime != null) this.durationEndTime.remove(playerName);
		BuffManager buffman = MagicSpells.getBuffManager();
		if (buffman != null) buffman.removeBuff(player, this);
		sendMessage(this.strFade, player, null);
		playSpellEffects(EffectPosition.DISABLED, player);
		turnOffBuff(player);
		cancelEffects(EffectPosition.CASTER, player.getUniqueId().toString());
	}
	
	protected abstract void turnOffBuff(Player player);
	
	@Override
	protected abstract void turnOff();
	
	@Override
	public boolean isBeneficialDefault() {
		return true;
	}
	
	public boolean isTargeted() {
		return this.targeted;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!isActive(player)) return;
		if (!isExpired(player)) return;
		turnOff(player);
	}
	
	public class DamageListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerDamage(EntityDamageEvent event) {
			Entity eventEntity = event.getEntity();
			if (cancelOnTakeDamage && eventEntity instanceof Player && isActive((Player)eventEntity)) {
				turnOff((Player)eventEntity);
			} else if (cancelOnGiveDamage && event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
				Entity evtDamager = evt.getDamager();
				if (evtDamager instanceof Player && isActive((Player)evtDamager)) {
					turnOff((Player)evtDamager);
				} else if (evtDamager instanceof Projectile && ((Projectile)evtDamager).getShooter() instanceof LivingEntity) {
					LivingEntity shooter = (LivingEntity)((Projectile)evtDamager).getShooter();
					if (shooter instanceof Player && isActive((Player)shooter)) turnOff((Player)shooter);
				}
			}
		}
		
	}
	
	public class DeathListener implements Listener {
		
		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (!isActive(player)) return;
			turnOff(player);
		}
		
	}
	
	public class TeleportListener implements Listener {
		
		@EventHandler(priority=EventPriority.LOWEST)
		public void onTeleport(PlayerTeleportEvent event) {
			Player player = event.getPlayer();
			if (isActive(player)) {
				Location locationFrom = event.getFrom();
				Location locationTo = event.getTo();
				if (LocationUtil.differentWorldDistanceGreaterThan(locationFrom, locationTo, 5)) {
					turnOff(player);
				}
			}
		}
		
	}
	
	public class ChangeWorldListener implements Listener {
		
		@EventHandler(priority=EventPriority.LOWEST)
		public void onChangeWorld(PlayerChangedWorldEvent event) {
			if (isActive(event.getPlayer())) {
				turnOff(event.getPlayer());
			}
		}
		
	}
	
	public class SpellCastListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onChangeWorld(SpellCastEvent event) {
			if (thisSpell != event.getSpell() && event.getSpellCastState() == SpellCastState.NORMAL && isActive(event.getCaster())) {
				turnOff(event.getCaster());
			}
		}
		
	}

	public class QuitListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onPlayerQuit(PlayerQuitEvent event) {
			if (isActive(event.getPlayer())) {
				turnOff(event.getPlayer());
			}
		}
		
	}
	
}
