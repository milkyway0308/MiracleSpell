package com.nisovin.magicspells.mana;

import com.nisovin.magicspells.util.compat.EventUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.util.PlayerNameUtils;

public class ManaBar {

	private String playerName;
	private ManaRank rank;
	private int maxMana;
	private int regenAmount;
	private String prefix;
	private ChatColor colorFull;
	private ChatColor colorEmpty;
	
	private int mana;
	
	public ManaBar(Player player, ManaRank rank) {
		this.playerName = player.getName().toLowerCase();		
		setRank(rank);		
	}
	
	public void setRank(ManaRank rank) {
		this.rank = rank;
		this.maxMana = rank.maxMana;
		this.regenAmount = rank.regenAmount;
		this.mana = rank.startingMana;
		setDisplayData(rank.prefix, rank.colorFull, rank.colorEmpty);
	}
	
	public Player getPlayer() {
		return PlayerNameUtils.getPlayerExact(playerName);
	}
	
	public ManaRank getManaRank() {
		return this.rank;
	}
	
	public int getMana() {
		return this.mana;
	}
	
	public int getMaxMana() {
		return this.maxMana;
	}
	
	public int getRegenAmount() {
		return this.regenAmount;
	}
	
	public void setMaxMana(int max) {
		this.maxMana = max;
		if (this.mana > this.maxMana) this.mana = this.maxMana;
	}
	
	public void setRegenAmount(int amount) {
		this.regenAmount = amount;
	}
	
	private void setDisplayData(String prefix, ChatColor colorFull, ChatColor colorEmpty) {
		this.prefix = prefix;
		this.colorFull = colorFull;
		this.colorEmpty = colorEmpty;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	public ChatColor getColorFull() {
		return this.colorFull;
	}
	public ChatColor getColorEmpty() {
		return this.colorEmpty;
	}
	
	public boolean has(int amount) {
		return this.mana >= amount;
	}
	
	public boolean changeMana(int amount, ManaChangeReason reason) {
		int newAmt = this.mana;
		
		if (amount > 0) {
			if (this.mana == this.maxMana) return false;
			newAmt += amount;
			if (newAmt > this.maxMana) newAmt = this.maxMana;
		} else if (amount < 0) {
			if (this.mana == 0) return false;
			newAmt += amount;
			if (newAmt < 0) newAmt = 0;
		}
		if (newAmt == this.mana) return false;
		
		newAmt = callManaChangeEvent(newAmt, reason);
		if (newAmt > this.maxMana) newAmt = this.maxMana;
		if (newAmt < 0) newAmt = 0;
		if (newAmt == this.mana) return false;
		this.mana = newAmt;
		return true;
	}
	
	public boolean setMana(int amount, ManaChangeReason reason) {
		int newAmt = amount;
		if (newAmt > this.maxMana) {
			newAmt = this.maxMana;
		} else if (newAmt < 0) {
			newAmt = 0;
		}
		
		newAmt = callManaChangeEvent(newAmt, reason);
		if (newAmt == this.mana) return false;
		this.mana = newAmt;
		return true;
	}
	
	public boolean regenerate() {
		if ((this.regenAmount > 0 && this.mana == this.maxMana) || (this.regenAmount < 0 && this.mana == 0)) return false;
		return changeMana(this.regenAmount, ManaChangeReason.REGEN);
	}
	
	private int callManaChangeEvent(int newAmt, ManaChangeReason reason) {
		Player player = getPlayer();
		if (player != null && player.isOnline()) {
			ManaChangeEvent event = new ManaChangeEvent(player, this.mana, newAmt, this.maxMana, reason);
			EventUtil.call(event);
			return event.getNewAmount();
		}
		return newAmt;
	}
	
}
