package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;
import org.bukkit.entity.Player;

public class SleepTicksVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getSleepTicks();
		return 0;
	}

}
