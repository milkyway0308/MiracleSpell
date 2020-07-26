package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public final class TargetedMultiSpell extends TargetedSpell implements TargetedEntitySpell, TargetedLocationSpell {

	private static final Pattern DELAY_PATTERN = Pattern.compile("DELAY [0-9]+");
	
	@ConfigData(field="check-individual-cooldowns", dataType="boolean", defaultValue="false")
	private boolean checkIndividualCooldowns;
	
	@ConfigData(field="require-entity-target", dataType="boolean", defaultValue="false")
	private boolean requireEntityTarget;
	
	@ConfigData(field="point-blank", dataType="boolean", defaultValue="false")
	private boolean pointBlank;
	
	@ConfigData(field="y-offset", dataType="int", defaultValue="0")
	private int yOffset;
	
	@ConfigData(field="cast-random-spell-instead", dataType="boolean", defaultValue="false")
	private boolean castRandomSpellInstead;
	
	@ConfigData(field="stop-on-fail", dataType="boolean", defaultValue="true")
	boolean stopOnFail;
	
	@ConfigData(field="spells", dataType="String[]", defaultValue="null")
	private List<String> spellList;
	
	private ArrayList<Action> actions;
	private Random random = new Random();
	
	public TargetedMultiSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.checkIndividualCooldowns = getConfigBoolean("check-individual-cooldowns", false);
		this.requireEntityTarget = getConfigBoolean("require-entity-target", false);
		this.pointBlank = getConfigBoolean("point-blank", false);
		this.yOffset = getConfigInt("y-offset", 0);
		this.castRandomSpellInstead = getConfigBoolean("cast-random-spell-instead", false);
		this.stopOnFail = getConfigBoolean("stop-on-fail", true);
		
		this.actions = new ArrayList<>();
		this.spellList = getConfigStringList("spells", null);
	}
	
	@Override
	public void initialize() {
		super.initialize();

		if (this.spellList != null) {
			for (String s : this.spellList) {
				if (RegexUtil.matches(DELAY_PATTERN, s)) {
					int delay = Integer.parseInt(s.split(" ")[1]);
					this.actions.add(new Action(delay));
				} else {
					Subspell spell = new Subspell(s);
					if (spell.process()) {
						this.actions.add(new Action(spell));
					} else {
						MagicSpells.error("No such spell '" + s + "' for multi-spell '" + internalName + '\'');
					}
				}
			}
		}
		this.spellList = null;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// Check cooldowns
			if (this.checkIndividualCooldowns) {
				for (Action action : this.actions) {
					if (!action.isSpell()) continue;
					if (!action.getSpell().getSpell().onCooldown(player)) continue;
					// A spell is on cooldown
					sendMessage(this.strOnCooldown, player, args);
					return PostCastAction.ALREADY_HANDLED;
				}
			}
			
			// Get target
			Location locTarget = null;
			LivingEntity entTarget = null;
			if (this.requireEntityTarget) {
				TargetInfo<LivingEntity> info = getTargetedEntity(player, power);
				if (info != null) {
					entTarget = info.getTarget();
					power = info.getPower();
				}
			} else if (this.pointBlank) {
				locTarget = player.getLocation();
			} else {
				Block b;
				try {
					b = getTargetedBlock(player, power);
					if (b != null && b.getType() != Material.AIR) locTarget = b.getLocation();
				} catch (IllegalStateException e) {
					DebugHandler.debugIllegalState(e);
					b = null;
				}
			}
			if (locTarget == null && entTarget == null) return noTarget(player);
			if (locTarget != null) locTarget.setY(locTarget.getY() + this.yOffset);
			
			boolean somethingWasDone = runSpells(player, entTarget, locTarget, power);
			
			if (!somethingWasDone) return noTarget(player);
			
			if (entTarget != null) {
				sendMessages(player, entTarget);
				return PostCastAction.NO_MESSAGES;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return runSpells(caster, null, target, power);
	}
	
	@Override
	public boolean castAtLocation(Location location, float power) {
		return runSpells(null, null, location, power);
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		return runSpells(caster, target, null, power);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return runSpells(null, target, null, power);
	}
	
	boolean runSpells(Player player, LivingEntity entTarget, Location locTarget, float power) {
		boolean somethingWasDone = false;
		if (!this.castRandomSpellInstead) {
			int delay = 0;
			Subspell spell;
			List<DelayedSpell> delayedSpells = new ArrayList<>();
			for (Action action : this.actions) {
				if (action.isDelay()) {
					delay += action.getDelay();
				} else if (action.isSpell()) {
					spell = action.getSpell();
					if (delay == 0) {
						boolean ok = castTargetedSpell(spell, player, entTarget, locTarget, power);
						if (ok) {
							somethingWasDone = true;
						} else {
							// Spell failed - exit loop
							if (this.stopOnFail) break;
							continue;
						}
					} else {
						DelayedSpell ds = new DelayedSpell(spell, player, entTarget, locTarget, power, delayedSpells);
						delayedSpells.add(ds);
						Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, ds, delay);
						somethingWasDone = true;
					}
				}
			}
		} else {
			Action action = this.actions.get(this.random.nextInt(this.actions.size()));
			if (action.isSpell()) {
				somethingWasDone = castTargetedSpell(action.getSpell(), player, entTarget, locTarget, power);
			} else {
				somethingWasDone = false;
			}
		}
		if (somethingWasDone) {
			if (player != null) {
				if (entTarget != null) {
					playSpellEffects(player, entTarget);
				} else if (locTarget != null) {
					playSpellEffects(player, locTarget);
				}
			} else {
				if (entTarget != null) {
					playSpellEffects(EffectPosition.TARGET, entTarget);
				} else if (locTarget != null) {
					playSpellEffects(EffectPosition.TARGET, locTarget);
				}
			}
		}
		return somethingWasDone;
	}
	
	boolean castTargetedSpell(Subspell spell, Player caster, LivingEntity entTarget, Location locTarget, float power) {
		boolean success = false;
		if (spell.isTargetedEntitySpell() && entTarget != null) {
			success = spell.castAtEntity(caster, entTarget, power);
		} else if (spell.isTargetedLocationSpell()) {
			if (entTarget != null) {
				success = spell.castAtLocation(caster, entTarget.getLocation(), power);
			} else if (locTarget != null) {
				success = spell.castAtLocation(caster, locTarget, power);
			}
		} else {
			success = spell.cast(caster, power) == PostCastAction.HANDLE_NORMALLY;
		}
		return success;
	}
	
	private class Action {
		
		private Subspell spell;
		private int delay;
		
		public Action(Subspell spell) {
			this.spell = spell;
			this.delay = 0;
		}
		
		public Action(int delay) {
			this.delay = delay;
			this.spell = null;
		}
		
		public boolean isSpell() {
			return this.spell != null;
		}
		
		public Subspell getSpell() {
			return this.spell;
		}
		
		public boolean isDelay() {
			return this.delay > 0;
		}
		
		public int getDelay() {
			return this.delay;
		}
		
	}
	
	private class DelayedSpell implements Runnable {
		
		private Subspell spell;
		private Player player;
		private LivingEntity entTarget;
		private Location locTarget;
		private float power;
		
		private List<DelayedSpell> delayedSpells;
		private boolean cancelled;
		
		public DelayedSpell(Subspell spell, Player player, LivingEntity entTarget, Location locTarget, float power, List<DelayedSpell> delayedSpells) {
			this.spell = spell;
			this.player = player;
			this.entTarget = entTarget;
			this.locTarget = locTarget;
			this.power = power;
			this.delayedSpells = delayedSpells;
			this.cancelled = false;
		}
		
		public void cancel() {
			this.cancelled = true;
			this.delayedSpells = null;
		}
		
		public void cancelAll() {
			for (DelayedSpell ds : this.delayedSpells) {
				if (ds == this) continue;
				ds.cancel();
			}
			this.delayedSpells.clear();
			cancel();
		}
		
		@Override
		public void run() {
			if (!this.cancelled) {
				if (this.player == null || this.player.isValid()) {
					boolean ok = castTargetedSpell(this.spell, this.player, this.entTarget, this.locTarget, this.power);
					this.delayedSpells.remove(this);
					if (!ok && stopOnFail) cancelAll();
				} else {
					cancelAll();
				}
			}
			this.delayedSpells = null;
		}
		
	}
	
}
