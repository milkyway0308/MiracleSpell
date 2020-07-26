package com.nisovin.magicspells.variables.meta;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;

public class FoodLevelVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getFoodLevel();
		return 0;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) p.setFoodLevel((int) amount);
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			p.setFoodLevel(p.getFoodLevel() + (int) amount);
			return true;
		}
		return false;
	}

}
