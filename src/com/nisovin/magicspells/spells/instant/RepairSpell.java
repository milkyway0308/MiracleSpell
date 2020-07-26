package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;

public class RepairSpell extends InstantSpell {

	private static final String REPAIR_SELECTOR_KEY_HELD = "held";
	private static final String REPAIR_SELECTOR_KEY_HOTBAR = "hotbar";
	private static final String REPAIR_SELECTOR_KEY_INVENTORY = "inventory";
	private static final String REPAIR_SELECTOR_KEY_HELMET = "helmet";
	private static final String REPAIR_SELECTOR_KEY_CHESTPLATE = "chestplate";
	private static final String REPAIR_SELECTOR_KEY_LEGGINGS = "leggings";
	private static final String REPAIR_SELECTOR_KEY_BOOTS = "boots";
	
	private int repairAmt;
	private String[] toRepair;
	private Set<Material> ignoreItems;
	private Set<Material> allowedItems;
	private String strNothingToRepair;
	
	public RepairSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.repairAmt = getConfigInt("repair-amount", 300);
		List<String> toRepairList = getConfigStringList("to-repair", null);
		if (toRepairList == null) toRepairList = new ArrayList<>();
		if (toRepairList.isEmpty()) toRepairList.add(REPAIR_SELECTOR_KEY_HELD);
		Iterator<String> iter = toRepairList.iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			
			if (!s.equals(REPAIR_SELECTOR_KEY_HELD) && !s.equals(REPAIR_SELECTOR_KEY_HOTBAR) && !s.equals(REPAIR_SELECTOR_KEY_INVENTORY) && !s.equals(REPAIR_SELECTOR_KEY_HELMET) && !s.equals(REPAIR_SELECTOR_KEY_CHESTPLATE) && !s.equals(REPAIR_SELECTOR_KEY_LEGGINGS) && !s.equals(REPAIR_SELECTOR_KEY_BOOTS)) {
				Bukkit.getServer().getLogger().severe("MagicSpells: repair: invalid to-repair option: " + s);
				iter.remove();
			}
		}
		this.toRepair = new String[toRepairList.size()];
		this.toRepair = toRepairList.toArray(this.toRepair);
		
		this.ignoreItems = EnumSet.noneOf(Material.class);
		List<String> list = getConfigStringList("ignore-items", null);
		if (list != null) {
			for (String s : list) {
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveItem(s);
				if (m == null) continue;
				Material material = m.getMaterial();
				if (material == null) continue;
				this.ignoreItems.add(material);
			}
		}
		if (this.ignoreItems.isEmpty()) this.ignoreItems = null;
		
		this.allowedItems = EnumSet.noneOf(Material.class);
		list = getConfigStringList("allowed-items", null);
		if (list != null) {
			for (String s : list) {
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveItem(s);
				if (m == null) continue;
				Material material = m.getMaterial();
				if (material == null) continue;
				this.allowedItems.add(material);
			}
		}
		if (this.allowedItems.isEmpty()) this.allowedItems = null;
		
		this.strNothingToRepair = getConfigString("str-nothing-to-repair", "Nothing to repair.");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int repaired = 0;
			for (String s : this.toRepair) {
				if (s.equals(REPAIR_SELECTOR_KEY_HELD)) {
					ItemStack item = HandHandler.getItemInMainHand(player);
					if (item == null) continue;
					if (!isRepairable(item.getType())) continue;
					if (item.getDurability() > 0) {
						item.setDurability(newDura(item));
						HandHandler.setItemInMainHand(player, item);
						repaired++;
					}
					continue;
				}
				
				if (s.equals(REPAIR_SELECTOR_KEY_HOTBAR) || s.equals(REPAIR_SELECTOR_KEY_INVENTORY)) {
					int start;
					int end;
					ItemStack[] items = player.getInventory().getContents();
					
					if (s.equals(REPAIR_SELECTOR_KEY_HOTBAR)) {
						start = 0; 
						end = 9;
					} else {
						start = 9; 
						end = 36;
					}
					
					for (int i = start; i < end; i++) {
						ItemStack item = items[i];
						if (item == null) continue;
						if (!isRepairable(item.getType())) continue;
						if (item.getDurability() > 0) {
							item.setDurability(newDura(item));
							items[i] = item;
							repaired++;
						}
					}
					player.getInventory().setContents(items);
					continue;
				}
				
				if (s.equals(REPAIR_SELECTOR_KEY_HELMET)) {
					ItemStack item = player.getInventory().getHelmet();
					if (item == null) continue;
					if (!isRepairable(item.getType())) continue;
					if (item.getDurability() > 0) {
						item.setDurability(newDura(item));
						player.getInventory().setHelmet(item);
						repaired++;
					}
					continue;
				}
				
				if (s.equals(REPAIR_SELECTOR_KEY_CHESTPLATE)) {
					ItemStack item = player.getInventory().getChestplate();
					if (item == null) continue;
					if (!isRepairable(item.getType())) continue;
					if (item.getDurability() > 0) {
						item.setDurability(newDura(item));
						player.getInventory().setChestplate(item);
						repaired++;
					}
					continue;
				}
				
				if (s.equals(REPAIR_SELECTOR_KEY_LEGGINGS)) {
					ItemStack item = player.getInventory().getLeggings();
					if (item == null) continue;
					if (!isRepairable(item.getType())) continue;
					if (item.getDurability() > 0) {
						item.setDurability(newDura(item));
						player.getInventory().setLeggings(item);
						repaired++;
					}
					continue;
				}
				
				if (s.equals(REPAIR_SELECTOR_KEY_BOOTS)) {
					ItemStack item = player.getInventory().getBoots();
					if (item == null) continue;
					if (!isRepairable(item.getType())) continue;
					if (item.getDurability() > 0) {
						item.setDurability(newDura(item));
						player.getInventory().setBoots(item);
						repaired++;
					}
					continue;
				}
			}
			if (repaired == 0) {
				sendMessage(this.strNothingToRepair, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private short newDura(ItemStack item) {
		short dura = item.getDurability();
		dura -= this.repairAmt;
		if (dura < 0) dura = 0;
		return dura;
	}
	
	// TODO is it version safe to check for the repairable interface on the item?
	// TODO move this to check the itemstack itself
	// TODO make sure the new behavior is safe with the unbreakable tag
	private boolean isRepairable(Material material) {
		if (this.ignoreItems != null && this.ignoreItems.contains(material)) return false;
		if (this.allowedItems != null && !this.allowedItems.contains(material)) return false;
		String s = material.name();
		return 
				material == Material.BOW ||
				material == Material.FLINT_AND_STEEL ||
				material == Material.SHEARS ||
				material == Material.FISHING_ROD ||
				s.endsWith("HELMET") ||
				s.endsWith("CHESTPLATE") ||
				s.endsWith("LEGGINGS") ||
				s.endsWith("BOOTS") ||
				s.endsWith("AXE") ||
				s.endsWith("HOE") ||
				s.endsWith("PICKAXE") ||
				s.endsWith("SPADE") ||
				s.endsWith("SWORD");
	}

}
