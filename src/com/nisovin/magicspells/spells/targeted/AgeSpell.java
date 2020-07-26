package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class AgeSpell extends TargetedSpell implements TargetedEntitySpell {

	private boolean applyAgeLock = false;
	private boolean setMaturity = false;
	private int rawAge = 0;
	
	public AgeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		applyAgeLock = getConfigBoolean("apply-age-lock", false);
		setMaturity = getConfigBoolean("set-maturity", true);
		rawAge = getConfigInt("age", 0);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetEntityInfo = getTargetedEntity(player, power);
			if (targetEntityInfo == null || targetEntityInfo.getTarget() == null || !(targetEntityInfo.getTarget() instanceof Ageable)) return noTarget(player);
			Ageable a = (Ageable) targetEntityInfo.getTarget();
			applyAgeChanges(a);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void applyAgeChanges(Ageable a) {
		if (setMaturity) a.setAge(rawAge);
		if (applyAgeLock) a.setAgeLock(true);
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (target instanceof Ageable) {
			applyAgeChanges((Ageable) target);
			return true;
		}
		return false;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return castAtEntity(null, target, power);
	}
	
}
