package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spells.instant.LeapSpell;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LeapingCondition extends Condition {

	LeapSpell leapSpell;
	
	@Override
	public boolean setVar(String var) {
		Spell spell = MagicSpells.getSpellByInternalName(var);
		if (!(spell instanceof LeapSpell)) return false;
		leapSpell = (LeapSpell) spell;
		return true;
	}

	@Override
	public boolean check(Player player) {
		return leapSpell.isJumping(player);
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
