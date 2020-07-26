package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class OffhandCooldownSpell extends InstantSpell {

	String spellToCheck;
	Spell spell;
	ItemStack item;
	
	List<Player> players = new ArrayList<>();
	
	public OffhandCooldownSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.spellToCheck = getConfigString("spell", "");
		if (isConfigString("item")) {
			this.item = Util.getItemStackFromString(getConfigString("item", "stone"));
		} else if (isConfigSection("item")) {
			this.item = Util.getItemStackFromConfig(getConfigSection("item"));
		}
	}
	
	@Override
	public void initialize() {
		super.initialize();
		this.spell = MagicSpells.getSpellByInternalName(this.spellToCheck);
		
		if (this.spell != null && this.item != null) {
			MagicSpells.scheduleRepeatingTask(new Runnable() {
				@Override
				public void run() {
					Iterator<Player> iter = players.iterator();
					while (iter.hasNext()) {
						Player p = iter.next();
						if (!p.isValid()) {
							iter.remove();
						} else {
							float cd = spell.getCooldown(p);
							int amt = 1;
							if (cd > 0) {
								amt = -(int)Math.ceil(cd);
							}
							PlayerInventory playerInventory = p.getInventory();
							ItemStack off = playerInventory.getItemInOffHand();
							if (off == null || !off.isSimilar(item)) {
								playerInventory.setItemInOffHand(item.clone());
							}
							playerInventory.getItemInOffHand().setAmount(amt);
						}
					}
				}
			}, TimeUtil.TICKS_PER_SECOND, TimeUtil.TICKS_PER_SECOND);
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			this.players.add(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
