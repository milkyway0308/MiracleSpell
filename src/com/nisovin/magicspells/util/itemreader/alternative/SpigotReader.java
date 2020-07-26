package com.nisovin.magicspells.util.itemreader.alternative;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class SpigotReader implements ItemConfigTransformer {
	
	private static final String READER_KEY = "external::spigot";
	private static final String DATA_KEY = "data";
	
	@Override
	public ItemStack deserialize(ConfigurationSection section) {
		if (section == null) return null;
		return section.getItemStack(DATA_KEY);
	}
	
	@Override
	public ConfigurationSection serialize(ItemStack itemStack) {
		YamlConfiguration configuration = new YamlConfiguration();
		configuration.set(DATA_KEY, itemStack);
		return configuration;
	}
	
	@Override
	public String getReaderKey() {
		return READER_KEY;
	}
	
}
