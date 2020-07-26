package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.IDisguiseManager;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class UndisguiseSpell extends TargetedSpell implements TargetedEntitySpell {

	IDisguiseManager manager;
	
	public UndisguiseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		manager = DisguiseSpell.getDisguiseManager();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (manager == null) return PostCastAction.ALREADY_HANDLED;
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> target = getTargetPlayer(player, power);
			if (target == null) return noTarget(player);
			undisguise(player, target.getTarget());
			sendMessages(player, target.getTarget());
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean undisguise(Player caster, Player player) {
		if (manager == null) return false;
		manager.removeDisguise(player);
		if (caster != null) {
			playSpellEffects(caster, player);
		} else {
			playSpellEffects(EffectPosition.TARGET, player);
		}
		return true;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		return target instanceof Player && undisguise(caster, (Player)target);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return target instanceof Player && undisguise(null, (Player)target);
	}

}
