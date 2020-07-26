package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;
import org.bukkit.entity.Player;

public class MaximumAirVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getMaximumAir();
		return 0;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) p.setMaximumAir((int) amount);
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			p.setMaximumAir(p.getMaximumAir() + (int) amount);
			return true;
		}
		return false;
	}

}
