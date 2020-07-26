package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetBooleanState;
import com.nisovin.magicspells.util.TargetInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FlySpell extends TargetedSpell implements TargetedEntitySpell {
	
	private TargetBooleanState targetBooleanState;
	
	public FlySpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.targetBooleanState = TargetBooleanState.getFromName(getConfigString("target-state", "toggle"));
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
			if (targetInfo == null) return noTarget(player);
			Player target = targetInfo.getTarget();
			if (target == null) return noTarget(player);
			target.setFlying(targetBooleanState.getBooleanState(target.isFlying()));
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!(target instanceof Player)) return false;
		((Player) target).setFlying(targetBooleanState.getBooleanState(((Player) target).isFlying()));
		return true;
	}
	
	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!(target instanceof Player)) return false;
		((Player) target).setFlying(targetBooleanState.getBooleanState(((Player) target).isFlying()));
		return true;
	}
	
}
