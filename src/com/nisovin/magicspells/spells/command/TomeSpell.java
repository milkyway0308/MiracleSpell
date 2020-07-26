package com.nisovin.magicspells.spells.command;

import java.util.List;
import java.util.regex.Pattern;

import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellLearnEvent.LearnSource;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

// TODO this should not be hardcoded to use a book
/**
 * Configuration fields:
 * <ul>
 * <li>cancel-read-on-learn: true</li>
 * <li>consume-book: false</li>
 * <li>allow-overwrite: false</li>
 * <li>default-uses: -1</li>
 * <li>max-uses: 5</li>
 * <li>require-teach-perm: true</li>
 * <li>str-usage: "Usage: While holding a book, /cast " + name + " <spell> [uses]"</li>
 * <li>str-no-spell: "You do not know a spell with that name."</li>
 * <li>str-cant-teach: "You cannot create a tome with that spell."</li>
 * <li>str-no-book: "You must be holding a book."</li>
 * <li>str-already-has-spell: "That book already contains a spell."</li>
 * <li>str-already-known: "You already know the %s spell."</li>
 * <li>str-cant-learn: "You cannot learn the spell in this tome."</li>
 * <li>str-learned: "You have learned the %s spell."</li>
 * </ul>
 */
public class TomeSpell extends CommandSpell {

	private static final Pattern INT_PATTERN = Pattern.compile("^[0-9]+$");
	
	private boolean cancelReadOnLearn;
	private boolean consumeBook;
	private boolean allowOverwrite;
	private int defaultUses;
	private int maxUses;
	private boolean requireTeachPerm;
	private String strUsage;
	private String strNoSpell;
	private String strCantTeach;
	private String strNoBook;
	private String strAlreadyHasSpell;
	private String strAlreadyKnown;
	private String strCantLearn;
	private String strLearned;
	
	public TomeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.cancelReadOnLearn = getConfigBoolean("cancel-read-on-learn", true);
		this.consumeBook = getConfigBoolean("consume-book", false);
		this.allowOverwrite = getConfigBoolean("allow-overwrite", false);
		this.defaultUses = getConfigInt("default-uses", -1);
		this.maxUses = getConfigInt("max-uses", 5);
		this.requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		this.strUsage = getConfigString("str-usage", "Usage: While holding a book, /cast " + this.name + " <spell> [uses]");
		this.strNoSpell = getConfigString("str-no-spell", "You do not know a spell with that name.");
		this.strCantTeach = getConfigString("str-cant-teach", "You cannot create a tome with that spell.");
		this.strNoBook = getConfigString("str-no-book", "You must be holding a book.");
		this.strAlreadyHasSpell = getConfigString("str-already-has-spell", "That book already contains a spell.");
		this.strAlreadyKnown = getConfigString("str-already-known", "You already know the %s spell.");
		this.strCantLearn = getConfigString("str-cant-learn", "You cannot learn the spell in this tome.");
		this.strLearned = getConfigString("str-learned", "You have learned the %s spell.");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spell spell;
			if (args == null || args.length == 0) {
				// Fail -- no args
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				spell = MagicSpells.getSpellByInGameName(args[0]);
				if (spell == null || spellbook == null || !spellbook.hasSpell(spell)) {
					// Fail -- no spell
					sendMessage(this.strNoSpell, player, args);
					return PostCastAction.ALREADY_HANDLED;
				} else if (this.requireTeachPerm && !MagicSpells.getSpellbook(player).canTeach(spell)) {
					sendMessage(strCantTeach, player, args);
					return PostCastAction.ALREADY_HANDLED;
				}
			}
			
			ItemStack item = HandHandler.getItemInMainHand(player);
			if (item.getType() != Material.WRITTEN_BOOK) {
				// Fail -- no book
				sendMessage(this.strNoBook, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			if (!this.allowOverwrite && getSpellDataFromTome(item) != null) {
				// Fail -- already has a spell
				sendMessage(this.strAlreadyHasSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				int uses = this.defaultUses;
				if (args.length > 1 && RegexUtil.matches(INT_PATTERN, args[1])) {
					uses = Integer.parseInt(args[1]);
				}
				item = createTome(spell, uses, item);
				HandHandler.setItemInMainHand(player, item);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public ItemStack createTome(Spell spell, int uses, ItemStack item) {
		if (this.maxUses > 0 && uses > this.maxUses) {
			uses = this.maxUses;
		} else if (uses < 0) {
			uses = this.defaultUses;
		}
		if (item == null) {
			item = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta bookMeta = (BookMeta)item.getItemMeta();
			bookMeta.setTitle(getName() + ": " + spell.getName());
			item.setItemMeta(bookMeta);
		}
		Util.setLoreData(item, this.internalName + ':' + spell.getInternalName() + (uses > 0 ? "," + uses : ""));
		return item;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		return null;
	}
	
	private String getSpellDataFromTome(ItemStack item) {
		String loreData = Util.getLoreData(item);
		if (loreData != null && loreData.startsWith(this.internalName + ':')) {
			return loreData.replace(this.internalName + ':', "");
		}
		return null;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!event.hasItem()) return;
		ItemStack item = event.getItem();
		if (item.getType() != Material.WRITTEN_BOOK) return;
		
		String spellData = getSpellDataFromTome(item);
		if (spellData == null || spellData.isEmpty()) return;
		
		String[] data = spellData.split(",");
		Spell spell = MagicSpells.getSpellByInternalName(data[0]);
		int uses = -1;
		if (data.length > 1) {
			uses = Integer.parseInt(data[1]);
		}
		Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
		if (spell == null) return;
		if (spellbook == null) return;
		
		if (spellbook.hasSpell(spell)) {
			// Fail -- already known
			sendMessage(formatMessage(this.strAlreadyKnown, "%s", spell.getName()), event.getPlayer(), MagicSpells.NULL_ARGS);
		} else if (!spellbook.canLearn(spell)) {
			// Fail -- can't learn
			sendMessage(formatMessage(this.strCantLearn, "%s", spell.getName()), event.getPlayer(), MagicSpells.NULL_ARGS);
		} else {
			// Call event
			SpellLearnEvent learnEvent = new SpellLearnEvent(spell, event.getPlayer(), LearnSource.TOME, HandHandler.getItemInMainHand(event.getPlayer()));
			EventUtil.call(learnEvent);
			if (learnEvent.isCancelled()) {
				// Fail -- plugin cancelled
				sendMessage(formatMessage(this.strCantLearn, "%s", spell.getName()), event.getPlayer(), MagicSpells.NULL_ARGS);
			} else {
				// Give spell
				spellbook.addSpell(spell);
				spellbook.save();
				sendMessage(formatMessage(this.strLearned, "%s", spell.getName()), event.getPlayer(), MagicSpells.NULL_ARGS);
				if (this.cancelReadOnLearn) event.setCancelled(true);
				
				// Remove use
				if (uses > 0) {
					uses--;
					if (uses > 0) {
						Util.setLoreData(item, this.internalName + ':' + data[0] + ',' + uses);
					} else {
						Util.removeLoreData(item);
					}
				}
				// Consume
				if (uses <= 0 && this.consumeBook) {
					HandHandler.setItemInMainHand(event.getPlayer(), null);
				}
				playSpellEffects(EffectPosition.DELAYED, event.getPlayer());
			}
		}
	}

}
