package com.nisovin.magicspells.spells.instant;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class CastAtMarkSpell extends InstantSpell {

	private MarkSpell markSpell;
	private String markSpellName;
	
	private String spellNameToCast;
	private Subspell spellToCast;
	
	private boolean initialized = false;
	
	private String strNoMark;
	
	public CastAtMarkSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		markSpellName = getConfigString("mark-spell", null);
		spellNameToCast = getConfigString("spell", null);
		strNoMark = getConfigString("str-no-mark", "You do not have a mark specified");
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (initialized) return;
		if (markSpellName == null) {
			MagicSpells.error(getLoggingSpellPrefix() + " mark-spell cannot be null");
			return;
		}
		if (spellNameToCast == null) {
			MagicSpells.error(getLoggingSpellPrefix() + " spell cannot be null");
			return;
		}
		Spell s = MagicSpells.getSpellByInternalName(markSpellName);
		if (!(s instanceof MarkSpell)) {
			MagicSpells.error(getLoggingSpellPrefix() + " Mark spell specified is not a mark spell");
			return;
		}
		
		markSpell = (MarkSpell)s;
		
		Subspell toCast = new Subspell(spellNameToCast);
		if (!toCast.process()) {
			MagicSpells.error(getLoggingSpellPrefix() + " Could not build subspell from " + spellNameToCast);
			return;
		}
		
		if (!toCast.isTargetedLocationSpell()) {
			MagicSpells.error(getLoggingSpellPrefix() + ' ' + toCast.getSpell().getInternalName() + " is not a targeted location spell");
			return;
		}
		
		spellToCast = toCast;
		initialized = true;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (!initialized) return PostCastAction.HANDLE_NORMALLY;

		if (state == SpellCastState.NORMAL) {
			Location effectiveMark = markSpell.getEffectiveMark(player);
			if (effectiveMark == null) {
				sendMessage(player, strNoMark);
				return PostCastAction.HANDLE_NORMALLY;
			}
			spellToCast.castAtLocation(player, effectiveMark, power);
			return PostCastAction.HANDLE_NORMALLY;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public void turnOff() {
		super.turnOff();
		markSpell = null;
		spellToCast = null;
		initialized = false;
	}

}
