package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.Util;

public class OffsetLocationSpell extends TargetedSpell implements TargetedLocationSpell{

	private Vector relativeOffset;
	private Vector absoluteOffset;
	
	Subspell spell;
	
	public OffsetLocationSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		relativeOffset = getConfigVector("relative-offset", "0,0,0");
		absoluteOffset = getConfigVector("absolute-offset", "0,0,0");
		
		spell = new Subspell(getConfigString("spell", ""));
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (spell != null) {
			boolean ok = spell.process();
			if (!ok || !spell.isTargetedLocationSpell()) {
				MagicSpells.error("Invalid spell on OffsetLocationSpell '" + name + '\'');
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location baseTargetLocation;
			TargetInfo<LivingEntity> entityTargetInfo = getTargetedEntity(player, power);
			if (entityTargetInfo != null && entityTargetInfo.getTarget() != null) {
				baseTargetLocation = entityTargetInfo.getTarget().getLocation();
			} else {
				baseTargetLocation = getTargetedBlock(player, power).getLocation();
			}
			if (baseTargetLocation == null) return noTarget(player);
			
			Location loc = Util.applyOffsets(baseTargetLocation, relativeOffset, absoluteOffset);
			if (loc != null) {
				spell.castAtLocation(player, loc, power);
				playSpellEffects(player, loc);
			} else {
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return spell.castAtLocation(caster, Util.applyOffsets(target, relativeOffset, absoluteOffset), power);
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		return castAtLocation(null, target, power);
	}
	
}
