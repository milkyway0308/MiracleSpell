package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.MagicConfig;

public final class MultiSpell extends InstantSpell {
	
	private static final Pattern RANGED_DELAY_PATTERN = Pattern.compile("DELAY [0-9]+ [0-9]+");
	private static final Pattern BASIC_DELAY_PATTERN = Pattern.compile("DELAY [0-9]+");
	
	private boolean castWithItem;
	private boolean castByCommand;
	private boolean checkIndividualCooldowns;
	private boolean castRandomSpellInstead;
	private boolean customSpellCastChance;
	private boolean enableIndividualChances;
	private List<String> spellList;
	private List<ActionChance> actions;
	private Random random = new Random();

	public MultiSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		this.castWithItem = getConfigBoolean("can-cast-with-item", true);
		this.castByCommand = getConfigBoolean("can-cast-by-command", true);
		this.checkIndividualCooldowns = getConfigBoolean("check-individual-cooldowns", false);
		this.castRandomSpellInstead = getConfigBoolean("cast-random-spell-instead", false);
		this.customSpellCastChance = getConfigBoolean("enable-custom-spell-cast-chance", false);
		this.enableIndividualChances = getConfigBoolean("enable-individual-chances", false);

		this.actions = new ArrayList<>();
		this.spellList = getConfigStringList("spells", null);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (this.spellList != null) {
			for (String s : this.spellList) {
				String[] parts = s.split(":");
				double chance = parts.length == 2 ? Double.parseDouble(parts[1]) : 0.0D;
				s = parts[0];
				if (RegexUtil.matches(RANGED_DELAY_PATTERN, s)) {
					String[] splits = s.split(" ");
					int minDelay = Integer.parseInt(splits[1]);
					int maxDelay = Integer.parseInt(splits[2]);
					this.actions.add(new ActionChance(new Action(minDelay, maxDelay), chance));
				} else if (RegexUtil.matches(BASIC_DELAY_PATTERN, s)) {
					int delay = Integer.parseInt(s.split(" ")[1]);
					this.actions.add(new ActionChance(new Action(delay), chance));
				} else {
					Subspell spell = new Subspell(s);
					if (spell.process()) {
						this.actions.add(new ActionChance(new Action(spell), chance));
					} else {
						MagicSpells.error("No such spell '" + s + "' for multi-spell '" + this.internalName + '\'');
					}
				}
			}
		}
		this.spellList = null;
	}

	@Override
	public Spell.PostCastAction castSpell(Player player, Spell.SpellCastState state, float power, String[] args) {
		if (state == Spell.SpellCastState.NORMAL) {
			if (!this.castRandomSpellInstead) {
				if (this.checkIndividualCooldowns) {
					for (ActionChance actionChance : this.actions) {
						Action action = actionChance.getAction();
						if (!(action.isSpell() && action.getSpell().getSpell().onCooldown(player))) continue;
						sendMessage(this.strOnCooldown, player, args);
						return Spell.PostCastAction.ALREADY_HANDLED;
					}
				}
				int delay = 0;
				for (ActionChance actionChance : actions) {
					Action action = actionChance.getAction();
					if (action.isDelay()) {
						delay += action.getDelay();
					} else if (action.isSpell()) {
						Subspell spell = action.getSpell();
						if (delay == 0) {
							spell.cast(player, power);
						} else {
							Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new DelayedSpell(spell, player, power), delay);
						}
					}
				}
			} else {
				int index;
				if (this.customSpellCastChance) {
					int total = 0;
					for (ActionChance actionChance : this.actions) {
						total = (int) Math.round(total + actionChance.getChance());
					}
					index = this.random.nextInt(total);
					int s = 0;
					int i = 0;
					while (s < index) {
						s = (int) Math.round(s + this.actions.get(i++).getChance());
					}
					Action action = this.actions.get(Math.max(0, i - 1)).getAction();
					if (action.isSpell()) {
						if (this.checkIndividualCooldowns && action.getSpell().getSpell().onCooldown(player)) {
							sendMessage(this.strOnCooldown, player, args);
							return Spell.PostCastAction.ALREADY_HANDLED;
						}
						action.getSpell().cast(player, power);
					}
				} else if (this.enableIndividualChances) {
					for (ActionChance actionChance : this.actions) {
						double chance = Math.random();
						if ((actionChance.getChance() / 100.0D > chance) && actionChance.getAction().isSpell()) {
							Action action = actionChance.getAction();
							if (this.checkIndividualCooldowns && action.getSpell().getSpell().onCooldown(player)) {
								sendMessage(this.strOnCooldown, player, args);
								return Spell.PostCastAction.ALREADY_HANDLED;
							}
							action.getSpell().cast(player, power);
						}
					}
				} else {
					Action action = this.actions.get(this.random.nextInt(this.actions.size())).getAction();
					if (this.checkIndividualCooldowns && action.getSpell().getSpell().onCooldown(player)) {
						sendMessage(this.strOnCooldown, player, args);
						return Spell.PostCastAction.ALREADY_HANDLED;
					}
					action.getSpell().cast(player, power);
				}
			}
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return Spell.PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castFromConsole(final CommandSender sender, final String[] args) {
		if (!this.castRandomSpellInstead) {
			int delay = 0;
			for (ActionChance actionChance : this.actions) {
				Action action = actionChance.getAction();
				if (action.isSpell()) {
					if (delay == 0) {
						action.getSpell().getSpell().castFromConsole(sender, args);
					} else {
						final Spell spell = action.getSpell().getSpell();
						MagicSpells.scheduleDelayedTask(() -> spell.castFromConsole(sender, args), delay);
					}
				} else if (action.isDelay()) {
					delay += action.getDelay();
				}
			}
		} else {
			int index;
			if (this.customSpellCastChance) {
				int total = 0;
				for (ActionChance actionChance : this.actions) {
					total = (int) Math.round(total + actionChance.getChance());
				}
				index = this.random.nextInt(total);
				int s = 0;
				int i = 0;
				while (s < index) {
					s = (int) Math.round(s + this.actions.get(i++).getChance());
				}
				Action action = this.actions.get(Math.max(0, i - 1)).getAction();
				if (action.isSpell()) {
					action.getSpell().getSpell().castFromConsole(sender, args);
				}
			} else if (this.enableIndividualChances) {
				for (ActionChance actionChance : this.actions) {
					double chance = Math.random();
					if ((actionChance.getChance() / 100.0D > chance) && actionChance.getAction().isSpell()) {
						actionChance.getAction().getSpell().getSpell().castFromConsole(sender, args);
					}
				}
			} else {
				Action action = this.actions.get(this.random.nextInt(this.actions.size())).getAction();
				if (action.isSpell()) {
					action.getSpell().getSpell().castFromConsole(sender, args);
				}
			}
		}
		return true;
	}

	@Override
	public boolean canCastWithItem() {
		return this.castWithItem;
	}

	@Override
	public boolean canCastByCommand() {
		return this.castByCommand;
	}

	private class Action {
		
		private Subspell spell;
		private int delay; // Also gonna serve as minimum delay
		private boolean isRangedDelay = false;
		private int maxDelay;

		public Action(Subspell spell) {
			this.spell = spell;
			this.delay = 0;
		}

		public Action(int delay) {
			this.delay = delay;
			this.spell = null;
		}
		
		public Action(int minDelay, int maxDelay) {
			this.delay = minDelay;
			this.maxDelay = maxDelay;
			this.spell = null;
			this.isRangedDelay = true;
		}

		public boolean isSpell() {
			return this.spell != null;
		}

		public Subspell getSpell() {
			return this.spell;
		}

		public boolean isDelay() {
			return this.delay > 0 || isRangedDelay;
		}

		private int getRandomDelay() {
			return random.nextInt(maxDelay - delay) + delay;
		}
		
		public int getDelay() {
			return isRangedDelay ? getRandomDelay() : delay;
		}
		
	}

	private class DelayedSpell implements Runnable {
		
		private Subspell spell;
		private String playerName;
		private float power;

		public DelayedSpell(Subspell spell, Player player, float power) {
			this.spell = spell;
			this.playerName = player.getName();
			this.power = power;
		}

		@Override
		public void run() {
			Player player = Bukkit.getPlayerExact(this.playerName);
			if (player != null && player.isValid()) {
				this.spell.cast(player, this.power);
			}
		}
		
	}

	class ActionChance {
		
		private MultiSpell.Action action;
		private double chance;

		public ActionChance(MultiSpell.Action action, double chance) {
			this.action = action;
			this.chance = chance;
		}

		public MultiSpell.Action getAction() {
			return this.action;
		}

		public double getChance() {
			return this.chance;
		}
		
	}
	
}
