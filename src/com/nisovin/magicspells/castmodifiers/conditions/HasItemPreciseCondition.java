package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

// Only accepts predefined items and uses a much stricter match
public class HasItemPreciseCondition extends Condition {

	private ItemStack itemStack = null;
	
	@Override
	public boolean setVar(String var) {
		var = var.trim();
		ItemStack item = Util.predefinedItems.get(var.trim());
		if (InventoryUtil.isNothing(item)) return false;
		this.itemStack = item.clone();
		return true;
	}

	@Override
	public boolean check(Player player) {
		return check(player.getInventory());
	}
	
	private boolean check(Inventory inventory) {
		if (inventory == null) return false;
		return Arrays.stream(inventory.getContents())
			.filter(item -> !InventoryUtil.isNothing(item))
			.anyMatch(item -> this.itemStack.isSimilar(item));
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target == null) return false;
		return target instanceof InventoryHolder && check(((InventoryHolder)target).getInventory());
	}
	
	@Override
	public boolean check(Player player, Location location) {
		Block target = location.getBlock();
		if (target == null) return false;
		
		BlockState targetState = target.getState();
		if (targetState == null) return false;
		return targetState instanceof InventoryHolder && check(((InventoryHolder)targetState).getInventory());
	}

}
