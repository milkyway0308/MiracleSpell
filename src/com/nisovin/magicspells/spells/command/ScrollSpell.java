package com.nisovin.magicspells.spells.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.nisovin.magicspells.Perm;
import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;
import com.nisovin.magicspells.util.Util;

/**
 * public class ScrollSpell extends {@link CommandSpell}
 * Configuration fields:
 * <ul>
 * <li>cast-for-free: true</li>
 * <li>ignore-cast-perm: false</li>
 * <li>bypass-normal-checks: false</li>
 * <li>default-uses: 5</li>
 * <li>max-uses: 10</li>
 * <li>item-id: paper</li>
 * <li>right-click-cast: true</li>
 * <li>left-click-cast: false</li>
 * <li>remove-scroll-when-depleted: true</li>
 * <li>charge-reagents-for-spell-per-charge: false</li>
 * <li>require-teach-perm: true</li>
 * <li>require-scroll-cast-perm-on-use: true</li>
 * <li>str-scroll-name: "Magic Scroll: %s"</li>
 * <li>str-scroll-subtext: "Uses remaining: %u"</li>
 * <li>str-usage: "You must hold a single blank paper \nand type /cast scroll <spell> <uses>."</li>
 * <li>str-no-spell: "You do not know a spell by that name."</li>
 * <li>str-cant-teach: "You cannot create a scroll with that spell."</li>
 * <li>str-on-use: "Spell Scroll: %s used. %u uses remaining."</li>
 * <li>str-use-fail: "Unable to use this scroll right now."</li>
 * <li>predefined-scrolls: null</li>
 * <ul>
*/
public class ScrollSpell extends CommandSpell {

	private static final Pattern CAST_ARGUMENT_USE_COUNT_PATTERN = Pattern.compile("^-?[0-9]+$");
	private static final Pattern SCROLL_DATA_USES_PATTERN = Pattern.compile("^[0-9]+$");
	
	private boolean castForFree;
	private boolean ignoreCastPerm;
	private boolean bypassNormalChecks;
	private int defaultUses;
	private int maxUses;
	private MagicMaterial itemType;
	private boolean rightClickCast;
	private boolean leftClickCast;
	private boolean removeScrollWhenDepleted;
	private boolean chargeReagentsForSpellPerCharge;
	private boolean requireTeachPerm;
	private boolean requireScrollCastPermOnUse;
	private boolean textContainsUses;
	private String strScrollName;
	private String strScrollSubtext;
	private String strUsage;
	private String strNoSpell;
	private String strCantTeach;
	private String strOnUse;
	private String strUseFail;
	
	private List<String> predefinedScrolls;
	private Map<Integer, Spell> predefinedScrollSpells;
	private Map<Integer, Integer> predefinedScrollUses;
		
	public ScrollSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.castForFree = getConfigBoolean("cast-for-free", true);
		this.ignoreCastPerm = getConfigBoolean("ignore-cast-perm", false);
		this.bypassNormalChecks = getConfigBoolean("bypass-normal-checks", false);
		this.defaultUses = getConfigInt("default-uses", 5);
		this.maxUses = getConfigInt("max-uses", 10);
		this.itemType = MagicSpells.getItemNameResolver().resolveItem(getConfigString("item-id", "paper"));
		this.rightClickCast = getConfigBoolean("right-click-cast", true);
		this.leftClickCast = getConfigBoolean("left-click-cast", false);
		this.removeScrollWhenDepleted = getConfigBoolean("remove-scroll-when-depleted", true);
		this.chargeReagentsForSpellPerCharge = getConfigBoolean("charge-reagents-for-spell-per-charge", false);
		this.requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		this.requireScrollCastPermOnUse = getConfigBoolean("require-scroll-cast-perm-on-use", true);
		this.strScrollName = getConfigString("str-scroll-name", "Magic Scroll: %s");
		this.strScrollSubtext = getConfigString("str-scroll-subtext", "Uses remaining: %u");
		this.strUsage = getConfigString("str-usage", "You must hold a single blank paper \nand type /cast scroll <spell> <uses>.");
		this.strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		this.strCantTeach = getConfigString("str-cant-teach", "You cannot create a scroll with that spell.");
		this.strOnUse = getConfigString("str-on-use", "Spell Scroll: %s used. %u uses remaining.");
		this.strUseFail = getConfigString("str-use-fail", "Unable to use this scroll right now.");
		
		this.predefinedScrolls = getConfigStringList("predefined-scrolls", null);
		
		this.textContainsUses = this.strScrollName.contains("%u") || this.strScrollSubtext.contains("%u");
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (this.predefinedScrolls != null && !this.predefinedScrolls.isEmpty()) {
			this.predefinedScrollSpells = new HashMap<>();
			this.predefinedScrollUses = new HashMap<>();
			for (String s : this.predefinedScrolls) {
				String[] data = s.split(" ");
				try {
					int id = Integer.parseInt(data[0]);
					Spell spell = MagicSpells.getSpellByInternalName(data[1]);
					int uses = this.defaultUses;
					if (data.length > 2) uses = Integer.parseInt(data[2]);
					if (id > 0 && spell != null) {
						this.predefinedScrollSpells.put(id, spell);
						this.predefinedScrollUses.put(id, uses);
					} else {
						MagicSpells.error("Scroll spell has invalid predefined scroll: " + s);
					}
				} catch (Exception e) {
					MagicSpells.error("Scroll spell has invalid predefined scroll: " + s);
				}
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				// Fail -- no args
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get item in hand
			ItemStack inHand = HandHandler.getItemInMainHand(player);
			if (inHand.getAmount() != 1 || !itemType.equals(inHand)) {
				// Fail -- incorrect item in hand
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get spell
			Spell spell = MagicSpells.getSpellByInGameName(args[0]);
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			if (spell == null || spellbook == null || !spellbook.hasSpell(spell)) {
				// Fail -- no such spell
				sendMessage(this.strNoSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;			
			}
			if (this.requireTeachPerm && !spellbook.canTeach(spell)) {
				sendMessage(this.strCantTeach, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get uses
			int uses = this.defaultUses;
			if (args.length > 1 && RegexUtil.matches(CAST_ARGUMENT_USE_COUNT_PATTERN, args[1])) {
				uses = Integer.parseInt(args[1]);
			}
			if (uses > this.maxUses || (this.maxUses > 0 && uses <= 0)) {
				uses = this.maxUses;
			}
			
			// Get additional reagent cost
			if (this.chargeReagentsForSpellPerCharge && uses > 0) {
				SpellReagents reagents = spell.getReagents().multiply(uses);
				if (!hasReagents(player, reagents)) {
					// Missing reagents
					sendMessage(this.strMissingReagents, player, args);
					return PostCastAction.ALREADY_HANDLED;
				}
				// Has reagents, so just remove them
				removeReagents(player, reagents);
			}
			
			// Create scroll
			inHand = createScroll(spell, uses, inHand);
			HandHandler.setItemInMainHand(player, inHand);
			
			// Done
			sendMessage(formatMessage(this.strCastSelf, "%s", spell.getName()), player, args);
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public ItemStack createScroll(Spell spell, int uses, ItemStack item) {
		if (item == null) item = this.itemType.toItemStack(1);
		item.setDurability((short)0);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.strScrollName.replace("%s", spell.getName()).replace("%u", (uses >= 0 ? uses + "" : "many"))));
		if (this.strScrollSubtext != null && !this.strScrollSubtext.isEmpty()) {
			List<String> lore = new ArrayList<>();
			lore.add(ChatColor.translateAlternateColorCodes('&', this.strScrollSubtext.replace("%s", spell.getName()).replace("%u", (uses >= 0 ? uses + "" : "many"))));
			meta.setLore(lore);
		}
		item.setItemMeta(meta);
		Util.setLoreData(item, this.internalName + ':' + spell.getInternalName() + (uses > 0 ? "," + uses : ""));
		item = MagicSpells.getVolatileCodeHandler().addFakeEnchantment(item);
		return item;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		String[] args = Util.splitParams(partial);
		if (args.length == 1) return tabCompleteSpellName(sender, args[0]);
		return null;
	}
	
	private String getSpellDataFromScroll(ItemStack item) {
		String loreData = Util.getLoreData(item);
		if (loreData != null && loreData.startsWith(this.internalName + ':')) {
			return loreData.replace(this.internalName + ':', "");
		}
		return null;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!actionAllowedForCast(event.getAction())) return;
		Player player = event.getPlayer();
		ItemStack inHand = HandHandler.getItemInMainHand(player);
		if (this.itemType.getMaterial() != inHand.getType() || inHand.getAmount() > 1) return;
		
		// Check for predefined scroll
		if (inHand.getDurability() > 0 && this.predefinedScrollSpells != null) {
			Spell spell = this.predefinedScrollSpells.get(Integer.valueOf(inHand.getDurability()));
			if (spell != null) {
				int uses = this.predefinedScrollUses.get(Integer.valueOf(inHand.getDurability()));
				inHand = createScroll(spell, uses, inHand);
				HandHandler.setItemInMainHand(player, inHand);
			}
		}
		
		// Get scroll data (spell and uses)
		String scrollDataString = getSpellDataFromScroll(inHand);
		if (scrollDataString == null || scrollDataString.isEmpty()) return;
		String[] scrollData = scrollDataString.split(",");
		Spell spell = MagicSpells.getSpellByInternalName(scrollData[0]);
		if (spell == null) return;
		int uses = 0;
		if (scrollData.length > 1 && RegexUtil.matches(SCROLL_DATA_USES_PATTERN, scrollData[1])) {
			uses = Integer.parseInt(scrollData[1]);
		}

		// Check for permission
		if (this.requireScrollCastPermOnUse && !MagicSpells.getSpellbook(player).canCast(this)) {
			sendMessage(this.strUseFail, player, MagicSpells.NULL_ARGS);
			return;
		}
				
		
		// Cast spell
		if (this.ignoreCastPerm && !Perm.CAST.has(player, spell)) {
			player.addAttachment(MagicSpells.plugin, Perm.CAST.getNode(spell), true, 1);
		}
		if (this.castForFree && !Perm.NOREAGENTS.has(player)) {
			player.addAttachment(MagicSpells.plugin, Perm.NOREAGENTS.getNode(), true, 1);
		}
		SpellCastState state;
		PostCastAction action;
		if (this.bypassNormalChecks) {
			state = SpellCastState.NORMAL;
			action = spell.castSpell(player, SpellCastState.NORMAL, 1.0F, null);
		} else {
			SpellCastResult result = spell.cast(player);
			state = result.state;
			action = result.action;
		}

		if (state == SpellCastState.NORMAL && action != PostCastAction.ALREADY_HANDLED) {
			// Remove use
			if (uses > 0) {
				uses -= 1;
				if (uses > 0) {
					inHand = createScroll(spell, uses, inHand);
					if (this.textContainsUses) {
						HandHandler.setItemInMainHand(player, inHand);
					}
				} else {
					if (this.removeScrollWhenDepleted) {
						HandHandler.setItemInMainHand(player, null);
					} else {
						HandHandler.setItemInMainHand(player, itemType.toItemStack(1));
					}
				}
			}
			
			// Send msg
			sendMessage(formatMessage(this.strOnUse, "%s", spell.getName(), "%u", uses >= 0 ? uses + "" : "many"), player, MagicSpells.NULL_ARGS);
		}
	}
	
	@EventHandler
	public void onItemSwitch(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack inHand = player.getInventory().getItem(event.getNewSlot());
		
		if (inHand == null || inHand.getType() != this.itemType.getMaterial()) return;
		
		// Check for predefined scroll
		if (inHand.getDurability() > 0 && this.predefinedScrollSpells != null) {
			Spell spell = this.predefinedScrollSpells.get(Integer.valueOf(inHand.getDurability()));
			if (spell != null) {
				int uses = this.predefinedScrollUses.get(Integer.valueOf(inHand.getDurability()));
				inHand = createScroll(spell, uses, inHand);
				player.getInventory().setItem(event.getNewSlot(), inHand);
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

}
