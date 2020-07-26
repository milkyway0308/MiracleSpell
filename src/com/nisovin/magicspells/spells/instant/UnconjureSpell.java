package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class UnconjureSpell extends InstantSpell {

	List<String> itemNames;
	List<ItemStack> items;
	
	public UnconjureSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.itemNames = getConfigStringList("items", null);
	}
	
	@Override
	public void initialize() {
		this.items = new ArrayList<>();
		for (String s : this.itemNames) {
			ItemStack i = Util.getItemStackFromString(s);
			if (i == null) continue;
			this.items.add(i);
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			ItemStack[] inv = player.getInventory().getContents();
			if (check(inv)) {
				player.getInventory().setContents(inv);
			}
			ItemStack[] armor = player.getInventory().getArmorContents();
			if (check(armor)) {
				player.getInventory().setArmorContents(armor);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean check(ItemStack[] inv) {
		boolean chg = false;
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] == null) continue;
			for (ItemStack item : this.items) {
				if (!item.isSimilar(inv[i])) continue;
				inv[i] = null;
				chg = true;
				break;
			}
		}
		return chg;
	}

}
