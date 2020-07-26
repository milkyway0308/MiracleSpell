package com.nisovin.magicspells.util.cmd;

import com.nisovin.magicspells.exception.MagicException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class ArgPlayerOfflineUuid extends Arg<OfflinePlayer> {
	
	public ArgPlayerOfflineUuid(String name) {
		super(name);
	}
	
	public ArgPlayerOfflineUuid(String name, OfflinePlayer defaultValue) {
		super(name, defaultValue);
	}
	
	@Override
	protected OfflinePlayer readValueInner(String input) throws MagicException {
		try {
			return Bukkit.getOfflinePlayer(UUID.fromString(input));
		} catch (Exception exception) {
			return this.getDefaultValue();
		}
	}
	
	@Override
	public Collection<OfflinePlayer> getAll() {
		return Arrays.asList(Bukkit.getOfflinePlayers());
	}
	
}
