package com.nisovin.magicspells.util;

import com.nisovin.magicspells.util.compat.CompatBasics;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MoneyHandler {
	
	private Economy economy;

	public MoneyHandler() {
		RegisteredServiceProvider<Economy> provider = CompatBasics.getServiceProvider(Economy.class);
		if (provider != null) this.economy = provider.getProvider();
	}
	
	public boolean hasMoney(Player player, float money) {
		if (this.economy == null) return false;
		return this.economy.has(player.getName(), money);
	}
	
	public void removeMoney(Player player, float money) {
		if (this.economy == null) return;
		this.economy.withdrawPlayer(player.getName(), money);
	}
	
	public void addMoney(Player player, float money) {
		if (this.economy == null) return;
		this.economy.depositPlayer(player.getName(), money);
	}
	
	public double checkMoney(Player player) {
		if (this.economy == null) return 0;
		return this.economy.getBalance(player.getName());
	}
	
}
