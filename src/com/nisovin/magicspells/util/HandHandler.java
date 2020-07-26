package com.nisovin.magicspells.util;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HandHandler {

	private static boolean initialized = false;
	private static boolean offhandExists;
	
	public static void initialize() {
		// Only initialize once
		if (initialized) return;
		
		try {
			PlayerInteractEvent event = new PlayerInteractEvent(null, Action.LEFT_CLICK_AIR, new ItemStack(Material.STONE, 1), null, BlockFace.UP, EquipmentSlot.OFF_HAND);
			event.getHand();
			offhandExists = true;
		} catch (Throwable t) {
			offhandExists = false;
		}
		initialized = true;
	}
	
	public static boolean isMainHand(PlayerInteractEvent event) {
		if (!offhandExists) return true;
		return event.getHand() == EquipmentSlot.HAND;
	}
	
	public static boolean isOffhand(PlayerInteractEvent event) {
		return !isMainHand(event);
	}
	
	public static boolean isMainHand(PlayerInteractEntityEvent event) {
		if (!offhandExists) return true;
		return event.getHand() == EquipmentSlot.HAND;
	}
	
	public static boolean isOffhand(PlayerInteractEntityEvent event) {
		return !isMainHand(event);
	}
	
	public static ItemStack getItemInMainHand(Player equip) {
		return getItemInMainHand(equip.getEquipment());
	}
	
	public static ItemStack getItemInMainHand(EntityEquipment equip) {
		if (offhandExists) return equip.getItemInMainHand();
		return equip.getItemInHand();
	}
	
	public static void setItemInMainHand(Player equip, ItemStack item) {
		setItemInMainHand(equip.getEquipment(), item);
	}
	
	public static void setItemInMainHand(EntityEquipment equip, ItemStack item) {
		if (offhandExists) {
			equip.setItemInMainHand(item);
		} else {
			equip.setItemInHand(item);
		}
	}
	
	public static void setItemInMainHandDropChance(Player equip, float chance) {
		setItemInMainHandDropChance(equip.getEquipment(), chance);
	}
	
	public static void setItemInMainHandDropChance(EntityEquipment equip, float chance) {
		if (offhandExists) {
			equip.setItemInMainHandDropChance(chance);
		} else {
			equip.setItemInHandDropChance(chance);
		}
	}
	
	public static float getItemInMainHandDropChance(Player equip) {
		return getItemInMainHandDropChance(equip.getEquipment());
	}
	
	public static float getItemInMainHandDropChance(EntityEquipment equip) {
		if (offhandExists) return equip.getItemInMainHandDropChance();
		return equip.getItemInHandDropChance();
	}
	
	public static ItemStack getItemInOffHand(Player equip) {
		return getItemInOffHand(equip.getEquipment());
	}
	
	public static ItemStack getItemInOffHand(EntityEquipment equip) {
		if (offhandExists) return equip.getItemInOffHand();
		// No offhand to get from
		return null;
	}
	
	public static void setItemInOffHand(Player equip, ItemStack item) {
		setItemInOffHand(equip.getEquipment(), item);
	}
	
	public static void setItemInOffHand(EntityEquipment equip, ItemStack item) {
		if (offhandExists) {
			equip.setItemInOffHand(item);
		} else {
			// No op
		}
	}
	
	public static void setItemInOffHandDropChance(Player equip, float chance) {
		setItemInOffHandDropChance(equip.getEquipment(), chance);
	}
	
	public static void setItemInOffHandDropChance(EntityEquipment equip, float chance) {
		if (offhandExists) {
			equip.setItemInOffHandDropChance(chance);
		} else {
			// No op
		}
	}
	
	public static float getItemInOffHandDropChance(Player player) {
		return getItemInOffHandDropChance(player.getEquipment());
	}
	
	public static float getItemInOffHandDropChance(EntityEquipment equip) {
		if (offhandExists) return equip.getItemInOffHandDropChance();
		return 0F;
	}
	
}
