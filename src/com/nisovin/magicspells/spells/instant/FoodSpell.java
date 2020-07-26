package com.nisovin.magicspells.spells.instant;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

// TODO allow saturation to optionally be controlled by power
// TODO allow food to be optionally controlled by power
// TODO add a max food option
public class FoodSpell extends InstantSpell {

	private int food;
	private float saturation;
	private float maxSaturation;
	
	public FoodSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.food = getConfigInt("food", 4);
		this.saturation = getConfigFloat("saturation", 2.5F);
		this.maxSaturation = getConfigFloat("max-saturation", 0F);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int f = player.getFoodLevel() + this.food;
			if (f > 20) f = 20;
			player.setFoodLevel(f);
			
			float s = player.getSaturation() + this.saturation;
			if (this.maxSaturation > 0 && this.saturation > this.maxSaturation) this.saturation = this.maxSaturation;
			player.setSaturation(s);
			
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
