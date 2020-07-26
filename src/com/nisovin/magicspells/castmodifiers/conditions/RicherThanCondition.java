package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;

/**
 * Condition check to see if a player has more money than the target
 * 
 * @author TheComputerGeek2
 */
public class RicherThanCondition extends Condition {
	
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
		if (!(target instanceof Player)) return true;
		Player playerTarget = (Player)target;
		return MagicSpells.getMoneyHandler().checkMoney(player) > MagicSpells.getMoneyHandler().checkMoney(playerTarget);
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
