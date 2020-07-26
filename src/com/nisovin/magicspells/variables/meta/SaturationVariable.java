package com.nisovin.magicspells.variables.meta;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;

public class SaturationVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getSaturation();
		return 0D;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) p.setSaturation((float) amount);
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			p.setSaturation(p.getSaturation() + (float) amount);
			return true;
		}
		return false;
	}

}
