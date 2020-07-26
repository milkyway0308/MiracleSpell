package com.nisovin.magicspells.spells.buff;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

/**
 * ManaRegenSpell<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             regen-mod-amt
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class ManaRegenSpell extends BuffSpell { 

	private int regenModAmt;

	private HashSet<String> regenning;

	public ManaRegenSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		this.regenModAmt = getConfigInt("regen-mod-amt", 3);
		this.regenning = new HashSet<>();
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		this.regenning.add(player.getName());
		return true;
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onManaRegenTick(ManaChangeEvent event) {
		Player p = event.getPlayer();
		if (isExpired(p)) return;
		if (!isActive(p)) return;
		if (!event.getReason().equals(ManaChangeReason.REGEN)) return;
		
		int newAmt = event.getNewAmount() + this.regenModAmt;
		if (newAmt > event.getMaxMana()) {
			newAmt = event.getMaxMana();
		} else if (newAmt < 0) {
			newAmt = 0;
		}
		event.setNewAmount(newAmt);
		addUseAndChargeCost(p);
	}

	@Override
	public void turnOffBuff(Player player) {
		this.regenning.remove(player.getName());
	}

	@Override
	protected void turnOff() {
		this.regenning.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return this.regenning.contains(player.getName());
	}

}
