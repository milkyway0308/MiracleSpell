package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;

public class EnchantSpell extends InstantSpell {
	
	protected Map<Enchantment, Integer> enchantments;
	
	public EnchantSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		enchantments = new HashMap<>();
		if (!configKeyExists("enchantments")) throw new NullPointerException("There must be a configuration section called enchantments");
		ConfigurationSection enchantSection = getConfigSection("enchantments");
		for (String key: enchantSection.getKeys(false)) {
			enchantments.put(Enchantment.getByName(key), enchantSection.getInt(key));
		}
	}
	
	@Override
	public PostCastAction castSpell(final Player player, SpellCastState state, final float power, String[] args) {
		ItemStack targetItem = HandHandler.getItemInMainHand(player);
		if (targetItem == null) return PostCastAction.ALREADY_HANDLED;
		enchant(targetItem);
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	void enchant(ItemStack item) {
		for (Enchantment e: enchantments.keySet()) {
			enchant(item, e, enchantments.get(e));
		}
	}
	
	void enchant(ItemStack item, Enchantment enchant, int level) {
		if (level <= 0) {
			// Remove the enchant
			item.removeEnchantment(enchant);
		} else {
			item.addEnchantment(enchant, level);
		}
	}

}
