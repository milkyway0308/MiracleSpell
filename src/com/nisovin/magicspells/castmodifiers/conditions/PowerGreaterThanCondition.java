package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.castmodifiers.IModifier;
import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class PowerGreaterThanCondition extends Condition implements IModifier {

	private float power;
	
	@Override
	public boolean apply(SpellCastEvent event) {
		return event.getPower() > power;
	}

	@Override
	public boolean apply(ManaChangeEvent event) {
		// No power to check
		return false;
	}

	@Override
	public boolean apply(SpellTargetEvent event) {
		return event.getPower() > power;
	}

	@Override
	public boolean apply(SpellTargetLocationEvent event) {
		return event.getPower() > power;
	}

	@Override
	public boolean apply(MagicSpellsGenericPlayerEvent event) {
		// No power to check
		return false;
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean setVar(String var) {
		if (var != null && !var.isEmpty()) {
			try {
				power = Float.parseFloat(var);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
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
