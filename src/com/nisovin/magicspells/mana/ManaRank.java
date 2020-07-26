package com.nisovin.magicspells.mana;

import org.bukkit.ChatColor;

public class ManaRank {
	
	String name;
	int startingMana;
	int maxMana;
	int regenAmount;
	String prefix;
	ChatColor colorFull;
	ChatColor colorEmpty;
	
	@Override
	public String toString() {
		return "ManaRank:["
			+ "name=" + this.name
			+ ",startingMana=" + this.startingMana
			+ ",maxMana=" + this.maxMana
			+ ",regenAmount=" + this.regenAmount
			+ ",prefix=" + this.prefix
			+ ",colorFull=" + this.colorFull
			+ ",colorEmpty=" + this.colorEmpty
			+ ']';
	}
	
}
