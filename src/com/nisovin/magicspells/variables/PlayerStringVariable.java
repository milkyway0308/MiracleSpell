package com.nisovin.magicspells.variables;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class PlayerStringVariable extends PlayerVariable {

	Map<String, String> data;
	
	public PlayerStringVariable() {
		this.data = new HashMap<>();
	}
	
	@Override
	public void loadExtraData(ConfigurationSection section) {
		super.loadExtraData(section);
		this.defaultStringValue = section.getString("default-value", "");
	}
	
	@Override
	public String getStringValue(String player) {
		String ret = this.data.get(player);
		if (ret == null) ret = this.defaultStringValue;
		return ret;
	}
	
	@Override
	public void parseAndSet(String player, String textValue) {
		this.data.put(player, textValue);
	}
	
	@Override
	public void reset(String player) {
		this.data.remove(player);
	}
	
}
