package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.Util;

public class HasItemLessThanCondition extends Condition {

	ItemStack item;
	int count;
	
	@Override
	public boolean setVar(String var) {
		try {
			String[] s = var.split(":");
			item = Util.predefinedItems.get(s[0]);
			if (item == null) return false;
			count = Integer.parseInt(s[1]);
			return true;
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return check(player.getInventory());
	}
	
	private boolean check(Inventory inventory) {
		int c = 0;
		for (ItemStack i : inventory.getContents()) {
			if (i != null && i.isSimilar(item)) {
				c += i.getAmount();
			}
		}
		return c < count;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target == null) return false;
		if (target instanceof InventoryHolder) return check(((InventoryHolder)target).getInventory());
		return false;
	}

	@Override
	public boolean check(Player player, Location location) {
		Block target = location.getBlock();
		if (target == null) return false;
		
		BlockState targetState = target.getState();
		if (targetState == null) return false;
		
		if (targetState instanceof InventoryHolder) return check(((InventoryHolder)targetState).getInventory());
		
		return false;
	}

}
