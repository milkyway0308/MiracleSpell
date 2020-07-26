package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spells.BuffSpell;

public class BuffActiveCondition extends Condition {

	BuffSpell buff;
	
	@Override
	public boolean setVar(String var) {
		Spell spell = MagicSpells.getSpellByInternalName(var);
		if (spell instanceof BuffSpell) {
			buff = (BuffSpell)spell;
			return true;
		}
		return false;
	}

	@Override
	public boolean check(Player player) {
		return buff.isActiveAndNotExpired(player);
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return target instanceof Player && check((Player)target);
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
