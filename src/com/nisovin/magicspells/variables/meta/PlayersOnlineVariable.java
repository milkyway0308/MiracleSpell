package com.nisovin.magicspells.variables.meta;

import org.bukkit.Bukkit;

import com.nisovin.magicspells.variables.MetaVariable;

public class PlayersOnlineVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		return Bukkit.getServer().getOnlinePlayers().size();
	}

}
