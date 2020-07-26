package com.nisovin.magicspells.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nisovin.magicspells.mana.ManaChangeReason;

/**
 * This event is fired whenever a player's mana value is changed.
 *
 */
public class ManaChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
	private Player player;
	private int oldAmt;
	private int newAmt;
	private int maxMana;
	private ManaChangeReason reason;
	
	public ManaChangeEvent(Player player, int oldAmt, int newAmt, int maxMana, ManaChangeReason reason) {		
		this.player = player;
		this.oldAmt = oldAmt;
		this.newAmt = newAmt;
		this.maxMana = maxMana;
		this.reason = reason;
	}
	
	/**
	 * Gets the player whose mana is being modified
	 * @return the player
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Gets the amount of mana the player currently has.
	 * @return mana amount
	 */
	public int getOldAmount() {
		return this.oldAmt;
	}
	
	/**
	 * Gets the amount of mana the player will have.
	 * @return mana amount
	 */
	public int getNewAmount() {
		return this.newAmt;
	}
	
	/**
	 * Sets the amount of mana the player will have.
	 * @param mana amount
	 */
	public void setNewAmount(int mana) {
		this.newAmt = mana;
	}
	
	/**
	 * The maximum amount of mana the player can have.
	 * @return max mana
	 */
	public int getMaxMana() {
		return this.maxMana;
	}
	
	/**
	 * Gets the reason for the mana change (regen, spell cost, mana potion, or other).
	 * @return the reason
	 */
	public ManaChangeReason getReason() {
		return this.reason;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
