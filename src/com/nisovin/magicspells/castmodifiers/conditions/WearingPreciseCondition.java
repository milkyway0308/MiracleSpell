package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WearingPreciseCondition extends Condition {
	
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
		PlayerInventory inv = player.getInventory();
		if (check(inv.getHelmet())) return true;
		if (check(inv.getChestplate())) return true;
		if (check(inv.getLeggings())) return true;
		return check(inv.getBoots());
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) return check((Player)target);
		EntityEquipment equip = target.getEquipment();
		if (equip != null) {
			if (check(equip.getHelmet())) return true;
			if (check(equip.getChestplate())) return true;
			if (check(equip.getLeggings())) return true;
			return check(equip.getBoots());
		}
		return false;
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}
	
	private boolean check(ItemStack item) {
		return this.itemStack.isSimilar(item);
	}
	
}
