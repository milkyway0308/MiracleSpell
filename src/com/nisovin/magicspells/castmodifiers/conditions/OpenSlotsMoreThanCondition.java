package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class OpenSlotsMoreThanCondition extends Condition {

	int slots;
	
	@Override
	public boolean setVar(String var) {
		try {
			slots = Integer.parseInt(var);
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		int c = 0;
		ItemStack[] inv = player.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] == null) {
				c++;
			}
		}
		return c > slots;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return target instanceof Player && check((Player)target);
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
