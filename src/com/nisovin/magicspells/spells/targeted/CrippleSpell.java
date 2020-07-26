package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class CrippleSpell extends TargetedSpell implements TargetedEntitySpell {

	@ConfigData(field="use-slowness-effect", dataType="boolean", defaultValue="true", description="When set to true, the target will receive a slowness effect.")
	private boolean useSlownessEffect;
	
	@ConfigData(field="effect-strength", dataType="int", defaultValue="5")
	private int strength;
	
	@ConfigData(field="effect-duration", dataType="int", defaultValue="100")
	private int duration;
	
	@ConfigData(field="apply-portal-cooldown", defaultValue="false", dataType="boolean", description="When set to true, this spell will set the portal cooldown field of the target, if the duration of the new cooldown is longer than the existing cooldown.")
	private boolean applyPortalCooldown;
	
	@ConfigData(field="portal-cooldown-ticks", dataType="int", defaultValue="100", description="The number of ticks to set the target entity's portal cooldown to. This will not be used if it is less than the target's existing portal cooldown.")
	private int portalCooldown;
	
	public CrippleSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		useSlownessEffect = getConfigBoolean("use-slowness-effect", true);
		strength = getConfigInt("effect-strength", 5);
		duration = getConfigInt("effect-duration", 100);
		
		applyPortalCooldown = getConfigBoolean("apply-portal-cooldown", false);
		portalCooldown = getConfigInt("portal-cooldown-ticks", 100);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {		
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> target = getTargetedEntity(player, power);
			if (target == null) {
				// Fail
				return noTarget(player);
			}
			playSpellEffects(player, target.getTarget());
			cripple(target.getTarget(), power);
			sendMessages(player, target.getTarget());
			return PostCastAction.NO_MESSAGES;
		}
		
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) return false;
		playSpellEffects(caster, target);
		cripple(target, power);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!validTargetList.canTarget(target)) return false;
		playSpellEffects(EffectPosition.TARGET, target);
		cripple(target, power);
		return true;
	}
	
	private void cripple(LivingEntity target, float power) {
		if (target == null) return;
		
		// Slowness effect
		if (useSlownessEffect) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Math.round(duration * power), strength), true);
		}
		
		// Portal cooldown
		// FIXME provide a version safe handling for this
		if (applyPortalCooldown) {
			if (target.getPortalCooldown() < (int)(portalCooldown * power)) target.setPortalCooldown((int)(portalCooldown * power));
		}
	}

}
