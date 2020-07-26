package com.nisovin.magicspells.util.itemreader.alternative;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public interface ItemConfigTransformer {

	// Deserialize this section
	ItemStack deserialize(ConfigurationSection section);
	
	ConfigurationSection serialize(ItemStack itemStack);
	
	String getReaderKey();
	
}
