package com.nisovin.magicspells.variables;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import com.nisovin.magicspells.util.PlayerNameUtils;

public class PlayerVariable extends Variable {

	Map<String, Double> map = new HashMap<>();
	
	@Override
	public boolean modify(String player, double amount) {
		double value = getValue(player);
		double newvalue = value + amount;
		if (newvalue > this.maxValue) {
			newvalue = this.maxValue;
		} else if (newvalue < this.minValue) {
			newvalue = this.minValue;
		}
		if (value != newvalue) {
			this.map.put(player, newvalue);
			if (this.objective != null) this.objective.getScore(PlayerNameUtils.getOfflinePlayer(player)).setScore((int)newvalue);
			return true;
		}
		return false;
	}

	@Override
	public void set(String player, double amount) {
		this.map.put(player, amount);
		if (this.objective != null) this.objective.getScore(PlayerNameUtils.getOfflinePlayer(player)).setScore((int)amount);
	}

	@Override
	public double getValue(String player) {
		if (this.map.containsKey(player)) return this.map.get(player);
		return this.defaultValue;
	}

	@Override
	public void reset(String player) {
		this.map.remove(player);
		if (this.objective != null) this.objective.getScore(Bukkit.getOfflinePlayer(player)).setScore((int)this.defaultValue);
	}
	
}
