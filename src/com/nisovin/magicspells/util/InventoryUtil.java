package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

	public static final String SERIALIZATION_KEY_SIZE = "size";
	public static final String SERIALIZATION_KEY_TYPE = "type";
	public static final String SERIALIZATION_KEY_CONTENTS = "contents";
	public static final String SERIALIZATION_KEY_TITLE = "title";
	
	/*
	 * type: INVENTORY_TYPE/string
	 * size: integer
	 * title: string
	 * contents:
	 *     slot number: serialized itemstack
	 *     slot number: serialized itemstack
	 */
	public static Map<Object, Object> serializeInventoryContents(Inventory inv) {
		Map<Object, Object> ret = new HashMap<>();
		ItemStack[] contents = inv.getContents();
		String inventoryType = inv.getType().name();
		int size = inv.getSize();
		String title = inv.getTitle();
		
		// A map of slot to itemstack
		Map<Object, Object> serializedContents = createContentsMap(contents);
		
		ret.put(SERIALIZATION_KEY_SIZE, size);
		ret.put(SERIALIZATION_KEY_TYPE, inventoryType);
		ret.put(SERIALIZATION_KEY_CONTENTS, serializedContents);
		ret.put(SERIALIZATION_KEY_TITLE, title);
		
		return ret;
	}
	
	private static Map<Object, Object> createContentsMap(ItemStack[] items) {
		Map<Object, Object> serialized = new HashMap<>();
		int maxSlot = items.length - 1;
		for (int currentSlot = 0; currentSlot <= maxSlot; currentSlot++) {
			ItemStack currentItem = items[currentSlot];
			if (currentItem == null) continue;
			serialized.put(currentSlot, currentItem.serialize());
		}
		return serialized;
	}
	
	public static Inventory deserializeInventory(Map<Object, Object> serialized) {
		String strInventoryType = (String) serialized.get(SERIALIZATION_KEY_TYPE);
		int inventorySize = (Integer) serialized.get(SERIALIZATION_KEY_SIZE);
		String title = (String) serialized.get(SERIALIZATION_KEY_TITLE);
		Inventory ret;
		if (strInventoryType.equals(InventoryType.CHEST.name())) {
			ret = Bukkit.createInventory(null, inventorySize, title);
		} else {
			ret = Bukkit.createInventory(null, InventoryType.valueOf(strInventoryType), title);
		}
		
		// Handle the item contents
		Map<Object, Object> serializedItems = (Map<Object, Object>) serialized.get(SERIALIZATION_KEY_CONTENTS);
		ret.setContents(deserializeContentsMap(serializedItems, inventorySize));
		
		return ret;
	}
	
	private static ItemStack[] deserializeContentsMap(Map<Object, Object> contents, int size) {
		ItemStack[] ret = new ItemStack[size];
		
		// Can we exit early?
		if (contents == null) return ret;
		
		for (int i = 0; i < size; i++) {
			Map<String, Object> serializedStack = (Map<String, Object>) contents.get(i);
			if (serializedStack == null) continue;
			ret[i] = ItemStack.deserialize(serializedStack);
		}
		
		return ret;
	}
	
	public static boolean isNothing(ItemStack itemStack) {
		if (itemStack == null) return true;
		if (itemStack.getType() == Material.AIR) return true;
		return itemStack.getAmount() == 0;
	}
	
}
