package com.nisovin.magicspells.spells.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.nisovin.magicspells.Perm;
import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.materials.ItemNameResolver;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;
import com.nisovin.magicspells.util.Util;

// Advanced perm is for specifying the number of uses if it isn't normally allowed
public class ImbueSpell extends CommandSpell {

	private static final Pattern CAST_ARG_USES_PATTERN  = Pattern.compile("[0-9]+");
	
	private String key;
	
	private int defaultUses;
	private int maxUses;
	private boolean allowSpecifyUses;
	private boolean chargeReagentsForSpellPerUse;
	private boolean requireTeachPerm;
	private boolean consumeItem;
	private boolean rightClickCast;
	private boolean leftClickCast;
	private Set<Material> allowedItemTypes;
	private List<MagicMaterial> allowedItemMaterials;
	private boolean nameAndLoreHasUses;
	
	private String strItemName;
	private String strItemLore;
	private String strUsage;
	private String strCantImbueItem;
	private String strCantImbueSpell;
	
	public ImbueSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.key = "Imb" + this.internalName;
		this.defaultUses = getConfigInt("default-uses", 5);
		this.maxUses = getConfigInt("max-uses", 10);
		this.allowSpecifyUses = getConfigBoolean("allow-specify-uses", true);
		this.chargeReagentsForSpellPerUse = getConfigBoolean("charge-reagents-for-spell-per-use", true);
		this.requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		this.consumeItem = getConfigBoolean("consume-item", false);
		this.rightClickCast = getConfigBoolean("right-click-cast", false);
		this.leftClickCast = getConfigBoolean("left-click-cast", true);
		
		this.allowedItemTypes = new HashSet<>();
		this.allowedItemMaterials = new ArrayList<>();
		List<String> allowed = getConfigStringList("allowed-items", null);
		if (allowed != null) {
			ItemNameResolver resolver = MagicSpells.getItemNameResolver();
			for (String s : allowed) {
				MagicMaterial m = resolver.resolveItem(s);
				if (m == null) continue;
				this.allowedItemTypes.add(m.getMaterial());
				this.allowedItemMaterials.add(m);
			}
		}
		
		this.strItemName = getConfigString("str-item-name", "");
		this.strItemLore = getConfigString("str-item-lore", "Imbued: %s");
		this.strUsage = getConfigString("str-usage", "Usage: /cast imbue <spell> [uses]");
		this.strCantImbueItem = getConfigString("str-cant-imbue-item", "You can't imbue that item.");
		this.strCantImbueSpell = getConfigString("str-cant-imbue-spell", "You can't imbue that spell.");
		
		this.nameAndLoreHasUses = this.strItemName.contains("%u") || this.strItemLore.contains("%u");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				// Usage
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get item
			ItemStack inHand = HandHandler.getItemInMainHand(player);
			if (!this.allowedItemTypes.contains(inHand.getType())) {
				// Disallowed item
				sendMessage(this.strCantImbueItem, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			boolean allowed = false;
			for (MagicMaterial m : this.allowedItemMaterials) {
				if (m.equals(inHand)) {
					allowed = true;
					break;
				}
			}
			if (!allowed) {
				// Disallowed item
				sendMessage(this.strCantImbueItem, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Check for already imbued
			if (getImbueData(inHand) != null) {
				// Already imbued
				sendMessage(this.strCantImbueItem, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get spell
			Spell spell = MagicSpells.getSpellByInGameName(args[0]);
			if (spell == null) {
				// No spell
				sendMessage(this.strCantImbueSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (!MagicSpells.getSpellbook(player).hasSpell(spell)) {
				// Doesn't know spell
				sendMessage(this.strCantImbueSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Check teach perm
			if (requireTeachPerm && !MagicSpells.getSpellbook(player).canTeach(spell)) {
				// Can't teach
				sendMessage(this.strCantImbueSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get uses
			int uses = this.defaultUses;
			if (args.length > 1 && RegexUtil.matches(CAST_ARG_USES_PATTERN, args[1]) && (this.allowSpecifyUses || Perm.ADVANCED_IMBUE.has(player))) {
				uses = Integer.parseInt(args[1]);
				if (uses > this.maxUses) {
					uses = this.maxUses;
				} else if (uses <= 0) {
					uses = 1;
				}
			}
			
			// Get additional reagent cost
			if (this.chargeReagentsForSpellPerUse && !Perm.NOREAGENTS.has(player)) {
				SpellReagents reagents = spell.getReagents().multiply(uses);
				if (!hasReagents(player, reagents)) {
					// Missing reagents
					sendMessage(this.strMissingReagents, player, args);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					// Has reagents, so just remove them
					removeReagents(player, reagents);
				}
			}
			
			// Imbue item
			setItemNameAndLore(inHand, spell, uses);
			setImbueData(inHand, spell.getInternalName() + ',' + uses);
			HandHandler.setItemInMainHand(player, inHand);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY) return;
		if (!event.hasItem()) return;
		Action action = event.getAction();
		if (!actionAllowedForCast(action)) return;
		ItemStack item = event.getItem();
		if (!this.allowedItemTypes.contains(item.getType())) return;
		
		boolean allowed = false;
		for (MagicMaterial m : this.allowedItemMaterials) {
			if (m.equals(item)) {
				allowed = true;
				break;
			}
		}
		if (!allowed) return;
		
		String imbueData = getImbueData(item);
		if (imbueData != null && !imbueData.isEmpty()) {
			String[] data = imbueData.split(",");
			Spell spell = MagicSpells.getSpellByInternalName(data[0]);
			int uses = Integer.parseInt(data[1]);
			
			if (spell != null && uses > 0) {
				spell.castSpell(event.getPlayer(), SpellCastState.NORMAL, 1.0F, MagicSpells.NULL_ARGS);
				uses--;
				if (uses <= 0) {
					if (this.consumeItem) {
						HandHandler.setItemInMainHand(event.getPlayer(), null);
					} else {
						Util.removeLoreData(item);
						if (this.nameAndLoreHasUses) {
							setItemNameAndLore(item, spell, 0);
						}
					}
				} else {
					if (this.nameAndLoreHasUses) {
						setItemNameAndLore(item, spell, uses);
					}
					setImbueData(item, spell.getInternalName() + ',' + uses);
				}
			} else {
				Util.removeLoreData(item);
			}
		}
	}
	
	private boolean actionAllowedForCast(Action action) {
		switch (action) {
			case RIGHT_CLICK_AIR:
			case RIGHT_CLICK_BLOCK:
				return this.rightClickCast;
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				return this.leftClickCast;
			default:
				return false;
		}
	}
	
	private void setItemNameAndLore(ItemStack item, Spell spell, int uses) {
		ItemMeta meta = item.getItemMeta();
		if (!this.strItemName.isEmpty()) {
			meta.setDisplayName(this.strItemName.replace("%s", spell.getName()).replace("%u", uses+""));
		}
		if (!this.strItemLore.isEmpty()) {
			meta.setLore(Arrays.asList(this.strItemLore.replace("%s", spell.getName()).replace("%u", uses+"")));
		}
		item.setItemMeta(meta);
	}
	
	private void setImbueData(ItemStack item, String data) {
		Util.setLoreData(item, this.key + ':' + data);
	}
	
	private String getImbueData(ItemStack item) {
		String s = Util.getLoreData(item);
		
		if (s != null && s.startsWith(this.key + ':')) {
			return s.replace(this.key + ':', "");
		}
		return null;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		return null;
	}

}
