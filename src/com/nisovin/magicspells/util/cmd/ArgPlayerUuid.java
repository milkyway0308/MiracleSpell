package com.nisovin.magicspells.util.cmd;

import com.nisovin.magicspells.exception.MagicException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class ArgPlayerUuid extends Arg<Player> {
	
	public ArgPlayerUuid(String name) {
		super(name);
	}
	
	public ArgPlayerUuid(String name, Player defaultValue) {
		super(name, defaultValue);
	}
	
	@Override
	protected Player readValueInner(String input) throws MagicException {
		try {
			return Bukkit.getPlayer(UUID.fromString(input));
		} catch (Exception exception) {
			return this.getDefaultValue();
		}
	}
	
	@Override
	public Collection<Player> getAll() {
		return (Collection<Player>)Bukkit.getOnlinePlayers();
	}
}
