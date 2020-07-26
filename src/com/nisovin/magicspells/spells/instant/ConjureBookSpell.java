package com.nisovin.magicspells.spells.instant;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

// The special position plays on the dropped items
// Replaces {{name}} with the user's username
// Replaces {{disp}} with the user's display name
// Replaces {{<integer>}} with the argument at index <integer>
public class ConjureBookSpell extends InstantSpell implements TargetedLocationSpell {

	private static final Pattern NAME_VARIABLE_PATTERN = Pattern.compile(Pattern.quote("{{name}}"));
	private static final Pattern DISPLAY_NAME_VARIABLE_PATTERN = Pattern.compile(Pattern.quote("{{disp}}"));
	
	boolean addToInventory;
	ItemStack book;
	private boolean projectileHasGravity;
	private int pickupDelay;
	
	public ConjureBookSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		addToInventory = getConfigBoolean("add-to-inventory", true);
		pickupDelay = getConfigInt("pickup-delay", 0);
		pickupDelay = Math.max(pickupDelay, 0);
		
		String title = getConfigString("title", "Book");
		String author = getConfigString("author", "Steve");
		List<String> pages = getConfigStringList("pages", null);
		List<String> lore = getConfigStringList("lore", null);
		projectileHasGravity = getConfigBoolean("gravity", true);
		
		book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setTitle(ChatColor.translateAlternateColorCodes('&', title));
		meta.setAuthor(ChatColor.translateAlternateColorCodes('&', author));
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++) {
				pages.set(i, ChatColor.translateAlternateColorCodes('&', pages.get(i)));
			}
			meta.setPages(pages);
		}
		if (lore != null) {
			for (int i = 0; i < lore.size(); i++) {
				lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
			}
			meta.setLore(lore);
		}
		book.setItemMeta(meta);
	}
	
	private ItemStack getBook(Player player, String[] args) {
		ItemStack item = book.clone();
		BookMeta meta = (BookMeta)item.getItemMeta();
		String title = meta.getTitle();
		String author = meta.getAuthor();
		List<String> lore = meta.getLore();
		List<String> pages = meta.getPages();
		
		if (player != null) {
			String playerName = player.getName();
			String playerDisplayName = player.getDisplayName();
			
			title = applyVariables(title, playerName, playerDisplayName);
			author = applyVariables(author, playerName, playerDisplayName);
			if (lore != null && !lore.isEmpty()) {
				for (int l = 0; l < lore.size(); l++) {
					lore.set(l, applyVariables(lore.get(l), playerName, playerDisplayName));
				}
			}
			if (pages != null && !pages.isEmpty()) {
				for (int p = 0; p < pages.size(); p++) {
					pages.set(p, applyVariables(pages.get(p), playerName, playerDisplayName));
				}
			}
		}
		
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				title = title.replace("{{" + i + "}}", args[i]);
				author = author.replace("{{" + i + "}}", args[i]);
				if (lore != null && !lore.isEmpty()) {
					for (int l = 0; l < lore.size(); l++) {
						lore.set(l, lore.get(l).replace("{{" + i + "}}", args[i]));
					}
				}
				if (pages != null && !pages.isEmpty()) {
					for (int p = 0; p < pages.size(); p++) {
						pages.set(p, pages.get(p).replace("{{" + i + "}}", args[i]));
					}
				}
			}
		}
		
		meta.setTitle(title);
		meta.setAuthor(author);
		meta.setLore(lore);
		meta.setPages(pages);
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			boolean added = false;
			ItemStack item = getBook(player, args);
			if (addToInventory) {
				if (HandHandler.getItemInMainHand(player) == null || HandHandler.getItemInMainHand(player).getType() == Material.AIR) {
					HandHandler.setItemInMainHand(player, item);
					added = true;
				} else {
					added = Util.addToInventory(player.getInventory(), item, false, false);
				}
			}
			if (!added) {
				Item dropped = player.getWorld().dropItem(player.getLocation(), item);
				dropped.setItemStack(item);
				dropped.setPickupDelay(pickupDelay);
				MagicSpells.getVolatileCodeHandler().setGravity(dropped, projectileHasGravity);
				playSpellEffects(EffectPosition.SPECIAL, dropped);
				//player.getWorld().dropItem(player.getLocation(), item).setItemStack(item);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return castAtLocation(target, power);
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		ItemStack item = book.clone();
		Item dropped = target.getWorld().dropItem(target, item);
		dropped.setItemStack(item);
		dropped.setPickupDelay(pickupDelay);
		MagicSpells.getVolatileCodeHandler().setGravity(dropped, projectileHasGravity);
		playSpellEffects(EffectPosition.SPECIAL, dropped);
		//target.getWorld().dropItem(target, item).setItemStack(item);
		return true;
	}
	
	private static String applyVariables(String raw, String playerName, String displayName) {
		// TODO have regexutil replace all on these instead
		raw = NAME_VARIABLE_PATTERN.matcher(raw).replaceAll(playerName);
		raw = DISPLAY_NAME_VARIABLE_PATTERN.matcher(raw).replaceAll(displayName);
		return raw;
	}
	
}
