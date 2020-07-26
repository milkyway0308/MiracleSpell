package com.nisovin.magicspells;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.util.RegexUtil;
import com.nisovin.magicspells.util.Util;

public class CastCommand implements CommandExecutor, TabCompleter {
	
	private static final Pattern LOOSE_PLAYER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
	private static final Pattern LOCATION_PATTERN = Pattern.compile("^[^,]+,-?[0-9.]+,-?[0-9.]+,-?[0-9.]+(,-?[0-9.]+,-?[0-9.]+)?$");
	
	MagicSpells plugin;
	boolean enableTabComplete;
	
	public CastCommand(MagicSpells plugin, boolean enableTabComplete) {
		this.plugin = plugin;
		this.enableTabComplete = enableTabComplete;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		try {
			if (command.getName().equalsIgnoreCase("magicspellcast")) {
				// /magicspellcast /c
				args = Util.splitParams(args);
				if (args == null || args.length == 0) {
					if (sender instanceof Player) {
						MagicSpells.sendMessage(plugin.strCastUsage, (Player)sender, MagicSpells.NULL_ARGS);
					} else {
						sender.sendMessage(plugin.textColor + plugin.strCastUsage);
					}
				} else if (Perm.FORCECAST.has(sender) && args[0].equals("forcecast") && args.length >= 3) {
					// /c forcecast command
					Player target = PlayerNameUtils.getPlayer(args[1]);
					if (target == null) {
						sender.sendMessage(plugin.textColor + "No matching player found");
						return true;
					}
					Spell spell = MagicSpells.getSpellByInGameName(args[2]);
					if (spell == null) {
						sender.sendMessage(plugin.textColor + "No such spell");
						return true;
					}
					String[] spellArgs = null;
					if (args.length > 3) spellArgs = Arrays.copyOfRange(args, 3, args.length);
					spell.cast(target, spellArgs);
					sender.sendMessage(plugin.textColor + "Player " + target.getName() + " forced to cast " + spell.getName());
					// End forcecast command handling
					
				} else if (Perm.RELOAD.has(sender) && args[0].equals("reload")) {
					// /c reload
					if (args.length == 1) {
						plugin.unload();
						plugin.load();
						sender.sendMessage(plugin.textColor + "MagicSpells config reloaded.");
					} else {
						List<Player> players = plugin.getServer().matchPlayer(args[1]);
						if (players.size() != 1) {
							sender.sendMessage(plugin.textColor + "Player not found.");
						} else {
							Player player = players.get(0);
							plugin.spellbooks.put(player.getName(), new Spellbook(player, plugin));
							sender.sendMessage(plugin.textColor + player.getName() + "'s spellbook reloaded.");
						}
					}
					// End /c reload handling
					
				} else if (Perm.RESET_COOLDOWN.has(sender) && args[0].equals("resetcd")) {
					// /c resetcd
					Player p = null;
					if (args.length > 1) {
						p = PlayerNameUtils.getPlayer(args[1]);
						if (p == null) {
							sender.sendMessage(plugin.textColor + "No matching player found");
							return true;
						}
					}
					for (Spell spell : plugin.spells.values()) {
						if (p != null) {
							spell.setCooldown(p, 0);
						} else {
							spell.getCooldowns().clear();
						}
					}
					sender.sendMessage(plugin.textColor + "Cooldowns reset" + (p != null ? " for " + p.getName() : ""));
					// End /c resetcd handling
					
				} else if (Perm.RESET_MANA.has(sender) && args[0].equals("resetmana") && args.length > 1 && plugin.mana != null) {
					// /c resetmana
					Player p = PlayerNameUtils.getPlayer(args[1]);
					if (p != null) {
						plugin.mana.createManaBar(p);
						sender.sendMessage(plugin.textColor + p.getName() + "'s mana reset.");
					}
					// End /c resetmana handling
					
				} else if (Perm.UPDATE_MANA_RANK.has(sender) && args[0].equals("updatemanarank") && args.length > 1 && plugin.mana != null) {
					// /c updatemanarank
					Player p = PlayerNameUtils.getPlayer(args[1]);
					if (p != null) {
						boolean updated = plugin.mana.updateManaRankIfNecessary(p);
						plugin.mana.showMana(p);
						if (updated) {
							sender.sendMessage(plugin.textColor + p.getName() + "'s mana rank updated.");
						} else {
							sender.sendMessage(plugin.textColor + p.getName() + "'s mana rank already correct.");
						}
					}
					// End /c updatemanarank handling
					
				} else if (Perm.SET_MAX_MANA.has(sender) && args[0].equalsIgnoreCase("setmaxmana") && args.length == 3 && plugin.mana != null) {
					// /c setmaxmana
					Player p = PlayerNameUtils.getPlayer(args[1]);
					if (p != null) {
						int amt = Integer.parseInt(args[2]);
						plugin.mana.setMaxMana(p, amt);
						sender.sendMessage(plugin.textColor + p.getName() + "'s max mana set to " + amt + '.');
					}
					// End /c setmaxmana handling
					
				} else if (Perm.MODIFY_MANA.has(sender) && args[0].equalsIgnoreCase("modifymana") && args.length == 3 && plugin.mana != null) {
					// /c modifymana
					Player p = PlayerNameUtils.getPlayer(args[1]);
					if (p != null) {
						int amt = Integer.parseInt(args[2]);
						plugin.mana.addMana(p, amt, ManaChangeReason.OTHER);
						sender.sendMessage(plugin.textColor + p.getName() + "'s mana modified by " + amt + '.');
					}
					// End /c modifymana handling
					
				} else if (Perm.SET_MANA.has(sender) && args[0].equalsIgnoreCase("setmana") && args.length == 3 && plugin.mana != null) {
					// /c setmana
					Player p = PlayerNameUtils.getPlayer(args[1]);
					if (p != null) {
						int amt = Integer.parseInt(args[2]);
						plugin.mana.setMana(p, amt, ManaChangeReason.OTHER);
						sender.sendMessage(plugin.textColor + p.getName() + "'s mana set to " + amt + '.');
					}
					// End /c setmana handling
					
				} else if (Perm.MODIFY_MANA.has(sender) && args[0].equals("modifyvariable") && args.length == 4) {
					// /c modifyvariable <var> <player> ((=|-)?)<value>
					String var = args[1];
					String player = args[2];
					boolean set = false;
					boolean multiply = false;
					boolean divide = false;
					double num = 0;
					String numString = args[3];
					
					// Possible operations
					//+-=*/
					
					if (numString.startsWith("*")) {
						multiply = true;
						numString = numString.substring(1);
					} else if (numString.startsWith("/")) {
						divide = true;
						numString = numString.substring(1);
					} else if (numString.startsWith("=")) {
						set = true;
						numString = numString.substring(1);
					} else if (numString.startsWith("+")) {
						numString = numString.substring(1);
					}
					
					if (!RegexUtil.matches(RegexUtil.DOUBLE_PATTERN, numString)) {
						boolean negate = false;
						if (numString.startsWith("-")) {
							negate = true;
							numString = numString.substring(1);
						}
						String targetPlayerName = player;
						if (numString.contains(":")) {
							String[] targetVarData = numString.split(":");
							targetPlayerName = targetVarData[0];
							numString = targetVarData[1];
						}
						num = MagicSpells.getVariableManager().getValue(numString, PlayerNameUtils.getPlayer(targetPlayerName));
						if (negate) {
							num *= -1;
						}
					} else {
						num = Double.parseDouble(numString);
					}
					
					if (multiply) {
						MagicSpells.getVariableManager().multiplyBy(var, PlayerNameUtils.getPlayer(player), num);
					} else if (divide) {
						MagicSpells.getVariableManager().divideBy(var, PlayerNameUtils.getPlayer(player), num);
					} else if (set) {
						MagicSpells.getVariableManager().set(var, player, num);
					} else {
						MagicSpells.getVariableManager().modify(var, player, num);
					}
					// End /c modifyvariable handling
					
					// TODO this can be weakened to inventoryholder
				} else if (Perm.MAGICITEM.has(sender) && args[0].equals("magicitem") && args.length > 1 && sender instanceof Player) {
					// /c magicitem
					ItemStack item = Util.getItemStackFromString(args[1]);
					if (item != null) {
						if (args.length > 2 && RegexUtil.matches(RegexUtil.SIMPLE_INT_PATTERN, args[2])) {
							item.setAmount(Integer.parseInt(args[2]));
						}
						((Player)sender).getInventory().addItem(item);
					}
					// End /c magicitem handling
					
				} else if (Perm.DOWNLOAD.has(sender) && args[0].equals("download") && args.length == 3) {
					// /c download
					File file = new File(plugin.getDataFolder(), "spells-" + args[1] + ".yml");
					if (file.exists()) {
						sender.sendMessage(plugin.textColor + "ERROR: The file spells-" + args[1] + ".yml already exists!");
					} else {
						boolean downloaded = Util.downloadFile(args[2], file);
						if (downloaded) {
							sender.sendMessage(plugin.textColor + "SUCCESS! You will need to do a /cast reload to load the new spells.");
						} else {
							sender.sendMessage(plugin.textColor + "ERROR: The file could not be downloaded.");
						}
					}
					// End /c download handling
					
				} else if (Perm.UPDATE.has(sender) && args[0].equals("update") && args.length == 3) {
					// /c update
					File file = new File(plugin.getDataFolder(), "update-" + args[1] + ".yml");
					boolean downloaded = Util.downloadFile(args[2], file);
					boolean abort = false;
					if (downloaded) {
						sender.sendMessage(plugin.textColor + "Update file successfully downloaded.");
						// Delete the existing file
						File old = new File(plugin.getDataFolder(), args[1] + ".yml");
						if (old.exists()) {
							boolean deleteSuccess = old.delete();
							if (deleteSuccess) {
								sender.sendMessage(plugin.textColor + "Old file successfully deleted.");
							} else {
								sender.sendMessage(plugin.textColor + "Old file could not be deleted.");
								sender.sendMessage(plugin.textColor + "Aborting update, please delete the update file: " + file.getName());
								abort = true;
							}
						} else {
							sender.sendMessage(plugin.textColor + "There was no old file to delete.");
						}
						
						if (!abort) {
							// Rename the update to the original file's name
							boolean renamingSuccess = file.renameTo(new File(plugin.getDataFolder(), args[1] + ".yml"));
							if (renamingSuccess) {
								sender.sendMessage(plugin.textColor + "Successfully renamed the update file to " + args[1] + ".yml");
								sender.sendMessage(plugin.textColor + "You will need to do a /cast reload to load the update.");
							} else {
								sender.sendMessage(plugin.textColor + "Failed to rename the update file, update failed");
							}
						}
					} else {
						sender.sendMessage(plugin.textColor + "Update file failed to download.");
					}
					// End /c update handling
					
				} else if (Perm.SAVESKIN.has(sender) && args[0].equals("saveskin") && args.length == 3) {
					// /c saveskin
					Player player = PlayerNameUtils.getPlayerExact(args[1]);
					if (player != null) {
						MagicSpells.getVolatileCodeHandler().saveSkinData(player, args[2]);
						sender.sendMessage("Skin data for player " + player.getName() + " saved as " + args[2]);
					}
					// End /c saveskin handling
					
				} else if (Perm.PROFILE.has(sender) && args[0].equals("profilereport")) {
					// /c profilereport
					sender.sendMessage(plugin.textColor + "Creating profiling report");
					MagicSpells.profilingReport();
					// End /c profilereport handling
					
				} else if (Perm.DEBUG.has(sender) && args[0].equals("debug")) {
					// /c debug
					plugin.debug = !plugin.debug;
					sender.sendMessage("MagicSpells: debug mode " + (plugin.debug ? "enabled" : "disabled"));
					// End /c debug handling
					
				} else if (Perm.CAST_AT.has(sender) && args[0].equals("castat")) {
					// Begin /c castat handling
					return CastAtSubCommand.onCommand(sender, command, label, args);
					// End /c castat handling
				} else if (sender instanceof Player) {
					Player player = (Player)sender;
					Spellbook spellbook = MagicSpells.getSpellbook(player);
					Spell spell = MagicSpells.getSpellByInGameName(args[0]);
					if (spell != null && (!spell.isHelperSpell() || player.isOp()) && spell.canCastByCommand() && spellbook.hasSpell(spell)) {
						if (spell.isValidItemForCastCommand(HandHandler.getItemInMainHand(player))) {
							String[] spellArgs = null;
							if (args.length > 1) {
								spellArgs = new String[args.length - 1];
								for (int i = 1; i < args.length; i++) {
									spellArgs[i - 1] = args[i];
								}
							}
							spell.cast(player, spellArgs);
						} else {
							MagicSpells.sendMessage(spell.getStrWrongCastItem(), player, null);
						}
					} else {
						MagicSpells.sendMessage(plugin.strUnknownSpell, player, null);
					}
				} else { // Not a player
					Spell spell = plugin.spellNames.get(args[0].toLowerCase());
					if (spell == null) {
						sender.sendMessage("Unknown spell.");
					} else {
						String[] spellArgs = null;
						if (args.length > 1) {
							spellArgs = new String[args.length - 1];
							for (int i = 1; i < args.length; i++) {
								spellArgs[i - 1] = args[i];
							}
						}
						boolean casted = false;
						if (sender instanceof BlockCommandSender) {
							if (spell instanceof TargetedLocationSpell) {
								Location loc = ((BlockCommandSender)sender).getBlock().getLocation().add(.5, .5, .5);
								if (spellArgs != null && spellArgs.length >= 3) {
									try {
										int x = Integer.parseInt(spellArgs[0]);
										int y = Integer.parseInt(spellArgs[1]);
										int z = Integer.parseInt(spellArgs[2]);
										float yaw = 0;
										float pitch = 0;
										if (spellArgs.length > 3) yaw = Float.parseFloat(spellArgs[3]);
										if (spellArgs.length > 4) pitch = Float.parseFloat(spellArgs[4]);
										loc.add(x, y, z);
										loc.setYaw(yaw);
										loc.setPitch(pitch);
									} catch (NumberFormatException e) {
										DebugHandler.debugNumberFormat(e);
									}
								}
								((TargetedLocationSpell)spell).castAtLocation(loc, 1.0F);
								casted = true;
							}
						}
						
						// On to trying to handle as a non player entity
						if (!casted) {
							if (sender instanceof Entity) {
								Entity senderEntity = (Entity) sender;
								
								// For now, just the targeted location spells
								if (spell instanceof TargetedLocationSpell) {
									Location loc = senderEntity.getLocation();
									if (spellArgs != null && spellArgs.length >= 3) {
										try {
											int x = Integer.parseInt(spellArgs[0]);
											int y = Integer.parseInt(spellArgs[1]);
											int z = Integer.parseInt(spellArgs[2]);
											float yaw = 0;
											float pitch = 0;
											if (spellArgs.length > 3) yaw = Float.parseFloat(spellArgs[3]);
											if (spellArgs.length > 4) pitch = Float.parseFloat(spellArgs[4]);
											loc.add(x, y, z);
											loc.setYaw(yaw);
											loc.setPitch(pitch);
										} catch (NumberFormatException e) {
											DebugHandler.debugNumberFormat(e);
										}
									}
									// And cast
									((TargetedLocationSpell) spell).castAtLocation(loc, 1.0F);
									casted = true;
								}
							}
						}
						if (!casted) {
							boolean ok = spell.castFromConsole(sender, spellArgs);
							if (!ok) {
								if ((spell instanceof TargetedEntitySpell || spell instanceof TargetedLocationSpell) && spellArgs != null && spellArgs.length == 1 && RegexUtil.matches(LOOSE_PLAYER_NAME_PATTERN, spellArgs[0])) {
									Player target = PlayerNameUtils.getPlayer(spellArgs[0]);
									if (target != null) {
										if (spell instanceof TargetedEntitySpell) {
											ok = ((TargetedEntitySpell)spell).castAtEntity(target, 1.0F);
										} else if (spell instanceof TargetedLocationSpell) {
											ok = ((TargetedLocationSpell)spell).castAtLocation(target.getLocation(), 1.0F);
										}
										if (ok) {
											sender.sendMessage("Spell casted!");
										} else {
											sender.sendMessage("Spell failed, probably can't be cast from console.");
										}
									} else {
										sender.sendMessage("Invalid target.");
									}
								} else if (spell instanceof TargetedLocationSpell && spellArgs != null && spellArgs.length == 1 && RegexUtil.matches(LOCATION_PATTERN, spellArgs[0])) {
									String[] locData = spellArgs[0].split(",");
									World world = Bukkit.getWorld(locData[0]);
									if (world != null) {
										Location loc = new Location(world, Float.parseFloat(locData[1]), Float.parseFloat(locData[2]), Float.parseFloat(locData[3]));
										if (locData.length > 4) loc.setYaw(Float.parseFloat(locData[4]));
										if (locData.length > 5) loc.setPitch(Float.parseFloat(locData[5]));
										ok = ((TargetedLocationSpell)spell).castAtLocation(loc, 1.0F);
										if (ok) {
											sender.sendMessage("Spell casted!");
										} else {
											sender.sendMessage("Spell failed, probably can't be cast from console.");
										}
									} else {
										sender.sendMessage("No such world.");
									}
								} else {
									sender.sendMessage("Cannot cast that spell from console.");
								}
							}
						}
					}
				}
				return true;
				// End /magicspellcast /c handling
				
			} else if (command.getName().equalsIgnoreCase("magicspellmana")) {
				// /magicspellmana
				if (plugin.enableManaBars && sender instanceof Player) {
					Player player = (Player)sender;
					plugin.mana.showMana(player, true);
				}
				return true;
				// End /magicspellmana handling
				
			} else if (command.getName().equalsIgnoreCase("magicspellxp")) {
				// /magicspellxp
				if (sender instanceof Player) {
					MagicXpHandler xpHandler = plugin.magicXpHandler;
					if (xpHandler != null) {
						xpHandler.showXpInfo((Player)sender);
					}
				}
				return true;
				// End /magicspellxp handling
				
			}
			return false;
		} catch (Exception ex) {
			MagicSpells.handleException(ex);
			sender.sendMessage(ChatColor.RED + "An error has occured.");
			return true;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (enableTabComplete && sender instanceof Player) {
			Spellbook spellbook = MagicSpells.getSpellbook((Player)sender);
			String partial = Util.arrayJoin(args, ' ');
			return spellbook.tabComplete(partial);
		}
		return null;
	}

}
