package com.nisovin.magicspells.util.cmd;

import com.nisovin.magicspells.exception.MagicException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ArgPlayerName extends Arg<Player> {
	
	public ArgPlayerName(String name) {
		super(name);
	}
	
	public ArgPlayerName(String name, Player defaultValue) {
		super(name, defaultValue);
	}
	
	@Override
	protected Player readValueInner(String input) throws MagicException {
		return Bukkit.getPlayer(input);
	}
	
	@Override
	public Collection<Player> getAll() {
		return (Collection<Player>)Bukkit.getOnlinePlayers();
	}
	
}
