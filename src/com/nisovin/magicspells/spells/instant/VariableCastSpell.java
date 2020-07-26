package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.entity.Player;

public class VariableCastSpell extends InstantSpell {
	
	private String variableName;
	private String strDoesntContainSpell;
	
	public VariableCastSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.variableName = getConfigString("variable-name", null);
		this.strDoesntContainSpell = getConfigString("str-doesnt-contain-spell", "You do not have a valid spell in memory");
	}
	
	@Override
	public void initialize() {
		// Super
		super.initialize();
		
		if (MagicSpells.getVariableManager().getVariable(this.variableName) == null) {
			MagicSpells.error("variable-name references an invalid variable for " + this.internalName);
		}
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// Just keep it clean for the players
			if (this.variableName == null) return PostCastAction.HANDLE_NORMALLY;
			String strValue = MagicSpells.getVariableManager().getVariable(this.variableName).getStringValue(player);
			Spell toCast = MagicSpells.getSpellByInternalName(strValue);
			if (toCast == null) {
				sendMessage(player, this.strDoesntContainSpell, args);
				return PostCastAction.NO_MESSAGES;
			}
			toCast.cast(player, power, args);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
}
