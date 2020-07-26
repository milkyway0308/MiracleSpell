package com.nisovin.magicspells.spells.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.nisovin.magicspells.Perm;
import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellLearnEvent.LearnSource;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.MagicLocation;

/**
 * public class SpellbookSpell extends {@link CommandSpell}
 * Configuration fields:
 * <ul>
 * <li>default-uses: -1</li>
 * <li>destroy-when-used-up: false</li>
 * <li>spellbook-block: bookshelf</li>
 * <li>str-usage: "Usage: /cast spellbook <spell> [uses]"</li>
 * <li>str-no-spell: "You do not know a spell by that name."</li>
 * <li>str-cant-teach: "You can't create a spellbook with that spell."</li>
 * <li>str-no-target: "You must target a bookcase to create a spellbook."</li>
 * <li>str-has-spellbook: "That bookcase already has a spellbook."</li>
 * <li>str-cant-destroy: "You cannot destroy a bookcase with a spellbook."</li>
 * <li>str-learn-error: ""</li>
 * <li>str-cant-learn: "You cannot learn the spell in this spellbook."</li>
 * <li>str-already-known: "You already know the %s spell."</li>
 * <li>str-learned: "You have learned the %s spell!"</li>
 * </ul>
 */
// Advanced perm is for being able to destroy spellbooks
// Op is currently required for using the reload
public class SpellbookSpell extends CommandSpell {
	
	private static final Pattern PATTERN_CAST_ARG_USAGE = Pattern.compile("^[0-9]+$");
	
	private int defaultUses;
	private boolean destroyBookcase;
	private MagicMaterial spellbookBlock;
	private String strUsage;
	private String strNoSpell;
	private String strCantTeach;
	private String strNoTarget;
	private String strHasSpellbook;
	private String strCantDestroy;
	private String strLearnError;
	private String strCantLearn;
	private String strAlreadyKnown;
	private String strLearned;
	
	private ArrayList<MagicLocation> bookLocations;
	private ArrayList<String> bookSpells;
	private ArrayList<Integer> bookUses;
	
	public SpellbookSpell(MagicConfig config, String spellName) {
		super(config,spellName);
		
		this.defaultUses = getConfigInt("default-uses", -1);
		this.destroyBookcase = getConfigBoolean("destroy-when-used-up", false);
		this.spellbookBlock = MagicSpells.getItemNameResolver().resolveBlock(getConfigString("spellbook-block", "bookshelf"));
		this.strUsage = getConfigString("str-usage", "Usage: /cast spellbook <spell> [uses]");
		this.strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		this.strCantTeach = getConfigString("str-cant-teach", "You can't create a spellbook with that spell.");
		this.strNoTarget = getConfigString("str-no-target", "You must target a bookcase to create a spellbook.");
		this.strHasSpellbook = getConfigString("str-has-spellbook", "That bookcase already has a spellbook.");
		this.strCantDestroy = getConfigString("str-cant-destroy", "You cannot destroy a bookcase with a spellbook.");
		this.strLearnError = getConfigString("str-learn-error", "");
		this.strCantLearn = getConfigString("str-cant-learn", "You cannot learn the spell in this spellbook.");
		this.strAlreadyKnown = getConfigString("str-already-known", "You already know the %s spell.");
		this.strLearned = getConfigString("str-learned", "You have learned the %s spell!");
		
		this.bookLocations = new ArrayList<>();
		this.bookSpells = new ArrayList<>();
		this.bookUses = new ArrayList<>();
		
		loadSpellbooks();
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length < 1 || args.length > 2 || (args.length == 2 && !RegexUtil.matches(PATTERN_CAST_ARG_USAGE, args[1]))) {
				// Fail: show usage string
				sendMessage(this.strUsage, player, args);
			} else {
				// Check for reload
				if (player.isOp() && args[0].equalsIgnoreCase("reload")) {
					this.bookLocations = new ArrayList<>();
					this.bookSpells = new ArrayList<>();
					this.bookUses = new ArrayList<>();
					loadSpellbooks();
					player.sendMessage("Spellbook file reloaded.");
					return PostCastAction.ALREADY_HANDLED;
				}
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				Spell spell = MagicSpells.getSpellByInGameName(args[0]);
				if (spellbook == null || spell == null || !spellbook.hasSpell(spell)) {
					// Fail: no such spell
					sendMessage(this.strNoSpell, player, args);
				} else if (!MagicSpells.getSpellbook(player).canTeach(spell)) {
					// Fail: can't teach
					sendMessage(this.strCantTeach, player, args);
				} else {
					Block target = getTargetedBlock(player, 10);
					if (target == null || !this.spellbookBlock.equals(target)) {
						// Fail: must target a bookcase
						sendMessage(this.strNoTarget, player, args);
						// FIXME there might be a type mismatch here
					} else if (this.bookLocations.contains(target.getLocation())) {
						// Fail: already a spellbook there
						sendMessage(this.strHasSpellbook, player, args);
					} else {
						// Create spellbook
						this.bookLocations.add(new MagicLocation(target.getLocation()));
						this.bookSpells.add(spell.getInternalName());
						if (args.length == 1) {
							this.bookUses.add(this.defaultUses);
						} else {
							this.bookUses.add(Integer.parseInt(args[1]));
						}
						saveSpellbooks();
						sendMessage(formatMessage(this.strCastSelf, "%s", spell.getName()), player, args);
						playSpellEffects(player, target.getLocation());
						return PostCastAction.NO_MESSAGES;
					}
				}
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void removeSpellbook(int index) {
		this.bookLocations.remove(index);
		this.bookSpells.remove(index);
		this.bookUses.remove(index);
		saveSpellbooks();
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.hasBlock() && this.spellbookBlock.equals(event.getClickedBlock()) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			MagicLocation loc = new MagicLocation(event.getClickedBlock().getLocation());
			if (this.bookLocations.contains(loc)) {
				event.setCancelled(true);
				Player player = event.getPlayer();
				int i = this.bookLocations.indexOf(loc);
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				Spell spell = MagicSpells.getSpellByInternalName(this.bookSpells.get(i));
				if (spellbook == null || spell == null) {
					// Fail: something's wrong
					sendMessage(this.strLearnError, player, MagicSpells.NULL_ARGS);
				} else if (!spellbook.canLearn(spell)) {
					// Fail: can't learn
					sendMessage(formatMessage(this.strCantLearn, "%s", spell.getName()), player, MagicSpells.NULL_ARGS);
				} else if (spellbook.hasSpell(spell)) {
					// Fail: already known
					sendMessage(formatMessage(this.strAlreadyKnown, "%s", spell.getName()), player, MagicSpells.NULL_ARGS);
				} else {
					// Call learn event
					SpellLearnEvent learnEvent = new SpellLearnEvent(spell, player, LearnSource.SPELLBOOK, event.getClickedBlock());
					EventUtil.call(learnEvent);
					if (learnEvent.isCancelled()) {
						// Fail: plugin cancelled it
						sendMessage(formatMessage(this.strCantLearn, "%s", spell.getName()), player, MagicSpells.NULL_ARGS);
					} else {
						// Teach the spell
						spellbook.addSpell(spell);
						spellbook.save();
						sendMessage(formatMessage(this.strLearned, "%s", spell.getName()), player, MagicSpells.NULL_ARGS);
						playSpellEffects(EffectPosition.DELAYED, player);
						int uses = this.bookUses.get(i);
						if (uses > 0) {
							uses--;
							if (uses == 0) {
								// Remove the spellbook
								if (this.destroyBookcase) {
									this.bookLocations.get(i).getLocation().getBlock().setType(Material.AIR);
								}
								removeSpellbook(i);
							} else {
								this.bookUses.set(i, uses);
							}
						}						
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		if (this.spellbookBlock.equals(event.getBlock())) {
			MagicLocation loc = new MagicLocation(event.getBlock().getLocation());
			if (this.bookLocations.contains(loc)) {
				if (event.getPlayer().isOp() || Perm.ADVANCEDSPELLBOOK.has(event.getPlayer())) {
					// Remove the bookcase
					int i = this.bookLocations.indexOf(loc);
					removeSpellbook(i);
				} else {
					// Cancel it
					event.setCancelled(true);
					sendMessage(this.strCantDestroy, event.getPlayer(), MagicSpells.NULL_ARGS);
				}
			}			
		}
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (sender.isOp() && args != null && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			this.bookLocations = new ArrayList<>();
			this.bookSpells = new ArrayList<>();
			this.bookUses = new ArrayList<>();
			loadSpellbooks();
			sender.sendMessage("Spellbook file reloaded.");
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		if (sender instanceof Player && !partial.contains(" ")) {
			return tabCompleteSpellName(sender, partial);
		}
		return null;
	}
	
	private void loadSpellbooks() {
		try {
			Scanner scanner = new Scanner(new File(MagicSpells.plugin.getDataFolder(), "books.txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.isEmpty()) {
					try {
						String[] data = line.split(":");
						MagicLocation loc = new MagicLocation(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
						int uses = Integer.parseInt(data[5]);
						this.bookLocations.add(loc);
						this.bookSpells.add(data[4]);
						this.bookUses.add(uses);
					} catch (Exception e) {
						MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Failed to load spellbook: " + line);
					}
				}
			}
		} catch (FileNotFoundException e) {
			//DebugHandler.debugFileNotFoundException(e);
		} 
	}
	
	private void saveSpellbooks() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(MagicSpells.plugin.getDataFolder(), "books.txt"), false));
			MagicLocation loc;
			for (int i = 0; i < this.bookLocations.size(); i++) {
				loc = this.bookLocations.get(i);
				writer.write(loc.getWorld() + ':' + (int)loc.getX() + ':' + (int)loc.getY() + ':' + (int)loc.getZ() + ':');
				writer.write(this.bookSpells.get(i) + ':' + this.bookUses.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Error saving spellbooks");
		}
	}
	
}
