package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.castmodifiers.IModifier;
import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class SpellBeneficialCondition extends Condition implements IModifier {

	@Override
	public boolean apply(SpellCastEvent event) {
		Spell spell = event.getSpell();
		return checkSpell(spell);
	}

	@Override
	public boolean apply(ManaChangeEvent event) {
		return false;
	}

	@Override
	public boolean apply(SpellTargetEvent event) {
		Spell spell = event.getSpell();
		return checkSpell(spell);
	}

	@Override
	public boolean apply(SpellTargetLocationEvent event) {
		Spell spell = event.getSpell();
		return checkSpell(spell);
	}

	@Override
	public boolean apply(MagicSpellsGenericPlayerEvent event) {
		return false;
	}
	
	private boolean checkSpell(Spell spell) {
		if (spell == null) return false;
		return spell.isBeneficial();
	}

	@Override
	public boolean setVar(String var) {
		return true;
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return false;
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
