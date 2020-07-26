package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import com.nisovin.magicspells.util.MagicConfig;

public class RandomSpell extends InstantSpell {

	static Random random = new Random();
	
	boolean pseudoRandom;
	boolean checkIndividualCooldowns;
	boolean checkIndividualModifiers;
	List<String> rawOptions;
	RandomOptionSet options;
	
	public RandomSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.pseudoRandom = getConfigBoolean("pseudo-random", true);
		this.checkIndividualCooldowns = getConfigBoolean("check-individual-cooldowns", true);
		this.checkIndividualModifiers = getConfigBoolean("check-individual-modifiers", true);
		this.rawOptions = getConfigStringList("spells", null);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		this.options = new RandomOptionSet();
		for (String s : this.rawOptions) {
			String[] split = s.split(" ");
			Subspell spell = new Subspell(split[0]);
			int weight = 0;
			try {
				weight = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {
				// No op
			}
			if (spell.process() && weight > 0) {
				this.options.add(new SpellOption(spell, weight));
			} else {
				MagicSpells.error("Invalid spell option on RandomSpell '" + this.internalName + "': " + s);
			}
		}
		
		this.rawOptions.clear();
		this.rawOptions = null;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			RandomOptionSet set = this.options;
			if (this.checkIndividualCooldowns || this.checkIndividualModifiers) {
				set = new RandomOptionSet();
				for (SpellOption o : this.options.randomOptionSetOptions) {
					if (this.checkIndividualCooldowns) {
						if (o.spell.getSpell().onCooldown(player)) continue;
					}
					if (this.checkIndividualModifiers) {
						ModifierSet modifiers = o.spell.getSpell().getModifiers();
						if (modifiers != null && !modifiers.check(player)) continue;
					}
					set.add(o);
				}
			}
			if (!set.randomOptionSetOptions.isEmpty()) {
				Subspell spell = set.choose();
				if (spell != null) return spell.cast(player, power);
				return PostCastAction.ALREADY_HANDLED;
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	class SpellOption {
		
		Subspell spell;
		int weight;
		int adjustedWeight;
		
		public SpellOption(Subspell spell, int weight) {
			this.spell = spell;
			this.weight = weight;
			this.adjustedWeight = weight;
		}
		
	}
	
	class RandomOptionSet {
		
		List<SpellOption> randomOptionSetOptions = new ArrayList<>();
		int total = 0;
		
		public void add(SpellOption option) {
			this.randomOptionSetOptions.add(option);
			this.total += option.adjustedWeight;
		}
		
		public Subspell choose() {
			int r = random.nextInt(this.total);
			int x = 0;
			Subspell spell = null;
			for (int i = 0; i < this.randomOptionSetOptions.size(); i++) {
				SpellOption o = this.randomOptionSetOptions.get(i);
				if (r < o.adjustedWeight + x && spell == null) {
					spell = o.spell;
					if (pseudoRandom) {
						o.adjustedWeight = 0;
					} else {
						break;
					}
				} else {
					x += o.adjustedWeight;
					if (pseudoRandom) {
						o.adjustedWeight += o.weight;
					}
				}
			}
			return spell;
		}
		
	}
	
}
