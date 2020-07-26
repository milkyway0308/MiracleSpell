package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.VariableMod;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

public class SlimeSizeSpell extends TargetedSpell implements TargetedEntitySpell {
	
	private VariableMod variableMod;
	private int minSize;
	private int maxSize;
	
	private static ValidTargetChecker isSlimeChecker = (LivingEntity entity) -> entity instanceof Slime;
	
	public SlimeSizeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		variableMod = new VariableMod(getConfigString("size", "=5"));
		
		minSize = getConfigInt("min-size", 0);
		
		// Just a little safety check
		if (minSize < 0) minSize = 0;
		
		maxSize = getConfigInt("max-size", 20);
		
		// Little sanity check
		if (maxSize < minSize) maxSize = minSize;
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
			if (targetInfo == null) return noTarget(player);
			LivingEntity targetEntity = targetInfo.getTarget();
			if (!(targetEntity instanceof Slime)) return noTarget(player);
			Slime slime = (Slime) targetEntity;
			double rawOutputValue = variableMod.getValue(player, null, slime.getSize());
			int finalSize = Util.clampValue(minSize, maxSize, (int)rawOutputValue);
			slime.setSize(finalSize);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public ValidTargetChecker getValidTargetChecker() {
		return isSlimeChecker;
	}
	
	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!(target instanceof Slime)) return false;
		Slime slime = (Slime) target;
		double rawOutputValue = variableMod.getValue(caster, null, slime.getSize());
		int finalSize = Util.clampValue(minSize, maxSize, (int)rawOutputValue);
		slime.setSize(finalSize);
		return true;
	}
	
	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return castAtEntity(null, target, power);
	}
	
}
