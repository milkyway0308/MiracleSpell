package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.EntityData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.data.DataEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class DataSpell extends TargetedSpell {
	
	
	private String variableName;
	private Function<? super LivingEntity, String> dataElement;
	
	public DataSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.variableName = getConfigString("variable-name", null);
		this.dataElement = DataEntity.getDataFunction(getConfigString("data-element", "uuid"));
		
	}
	
	@Override
	public void initialize() {
		if (variableName == null) {
			MagicSpells.error("variable-name is null for '" + internalName + "'");
			return;
		}
		
		if (dataElement == null) {
			MagicSpells.error("Invalid data-element defined for '" + internalName + "'");
		}

		if (MagicSpells.getVariableManager().getVariable(this.variableName) == null) {
			MagicSpells.error("invalid variable-name on '" + internalName + "'");
		}
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
			if (targetInfo == null) return noTarget(player);
			LivingEntity target = targetInfo.getTarget();
			if (target == null) return noTarget(player);
			
			String value = dataElement.apply(target);
			MagicSpells.getVariableManager().set(variableName, player, value);
			
			playSpellEffects(player, target);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
}
