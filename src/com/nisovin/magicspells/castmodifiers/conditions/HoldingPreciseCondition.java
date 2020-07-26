package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

// TODO this should be refactored along with the other 'has item' related conditions to reduce redundant code
public class HoldingPreciseCondition extends Condition {

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
		ItemStack item = HandHandler.getItemInMainHand(player);
		return check(item);
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		EntityEquipment equip = target.getEquipment();
		return equip != null && check(HandHandler.getItemInMainHand(equip));
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}
	
	private boolean check(ItemStack item) {
		return this.itemStack.isSimilar(item);
	}

}
