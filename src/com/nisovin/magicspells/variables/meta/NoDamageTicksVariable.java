package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;
import org.bukkit.entity.Player;

public class NoDamageTicksVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getNoDamageTicks();
		return 0;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) p.setNoDamageTicks((int) amount);
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			p.setNoDamageTicks(p.getNoDamageTicks() + (int) amount);
			return true;
		}
		return false;
	}

}
