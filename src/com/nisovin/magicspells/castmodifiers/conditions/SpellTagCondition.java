package com.nisovin.magicspells.castmodifiers.conditions;

import java.util.Set;

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

public class SpellTagCondition extends Condition implements IModifier {

	private String tag;
	
	@Override
	public boolean apply(SpellCastEvent event) {
		return checkSpell(event.getSpell());
	}

	@Override
	public boolean apply(ManaChangeEvent event) {
		return false;
	}

	@Override
	public boolean apply(SpellTargetEvent event) {
		return checkSpell(event.getSpell());
	}

	@Override
	public boolean apply(SpellTargetLocationEvent event) {
		return checkSpell(event.getSpell());
	}

	@Override
	public boolean apply(MagicSpellsGenericPlayerEvent event) {
		return false;
	}
	
	private boolean checkSpell(Spell spell) {
		if (spell == null) return false;
		Set<String> tags = spell.getTags();
		return checkWithTags(tags);
	}
	
	private boolean checkWithTags(Set<String> tags) {
		return tag != null && tags.contains(tag);
	}

	@Override
	public boolean setVar(String var) {
		if (var == null) return false;
		tag = var.trim();
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
