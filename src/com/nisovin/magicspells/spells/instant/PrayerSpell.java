package com.nisovin.magicspells.spells.instant;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import com.nisovin.magicspells.events.MagicSpellsEntityRegainHealthEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;

public class PrayerSpell extends InstantSpell {
	
	private double amountHealed;
	private String strAtFullHealth;
	private boolean checkPlugins;

	public PrayerSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.amountHealed = getConfigInt("amount-healed", 10);
		this.strAtFullHealth = getConfigString("str-at-full-health", "You are already at full health.");
		this.checkPlugins = getConfigBoolean("check-plugins", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (player.getHealth() >= player.getMaxHealth() && this.amountHealed > 0) {
				sendMessage(this.strAtFullHealth, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (player.isValid()) {
				double health = player.getHealth();
				if (health > 0) {
					double amt = this.amountHealed * power;
					if (this.checkPlugins && amt > 0) {
						MagicSpellsEntityRegainHealthEvent evt = new MagicSpellsEntityRegainHealthEvent(player, amt, RegainReason.CUSTOM);
						EventUtil.call(evt);
						if (evt.isCancelled()) return PostCastAction.ALREADY_HANDLED;
						amt = evt.getAmount();
					}
					health += amt;
					if (health > player.getMaxHealth()) {
						health = player.getMaxHealth();
					} else if (health < 0) {
						health = 0;
					}
					player.setHealth(health);
					playSpellEffects(EffectPosition.CASTER, player);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
