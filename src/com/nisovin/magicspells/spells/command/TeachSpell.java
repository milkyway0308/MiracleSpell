package com.nisovin.magicspells.spells.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellLearnEvent.LearnSource;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

/**
 * Configuration fields:
 * <ul>
 * <li>require-known-spell: true</li>
 * <li>str-usage: "Usage: /cast teach <target> <spell>"</li>
 * <li>str-no-target: "No such player."</li>
 * <li>str-no-spell: "You do not know a spell by that name."</li>
 * <li>str-cant-teach: "You can't teach that spell."</li>
 * <li>str-cant-learn: "That person cannot learn that spell."</li>
 * <li>str-already-known: "That person already knows that spell."</li>
 * <li>str-cast-target: "%a has taught you the %s spell."</li>
 * </ul>
 */
public class TeachSpell extends CommandSpell {

	private boolean requireKnownSpell;
	private String strUsage;
	private String strNoTarget;
	private String strNoSpell;
	private String strCantTeach;
	private String strCantLearn;
	private String strAlreadyKnown;
	private String strCastTarget;
	
	public TeachSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.requireKnownSpell = getConfigBoolean("require-known-spell", true);
		this.strUsage = getConfigString("str-usage", "Usage: /cast teach <target> <spell>");
		this.strNoTarget = getConfigString("str-no-target", "No such player.");
		this.strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		this.strCantTeach = getConfigString("str-cant-teach", "You can't teach that spell.");
		this.strCantLearn = getConfigString("str-cant-learn", "That person cannot learn that spell.");
		this.strAlreadyKnown = getConfigString("str-already-known", "That person already knows that spell.");
		this.strCastTarget = getConfigString("str-cast-target", "%a has taught you the %s spell.");
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length != 2) {
				// Fail: missing args
				sendMessage(this.strUsage, player, args);
			} else {
				List<Player> players = MagicSpells.plugin.getServer().matchPlayer(args[0]);
				if (players.size() != 1) {
					// Fail: no player match
					sendMessage(this.strNoTarget, player, args);
				} else {
					Spell spell = MagicSpells.getSpellByInGameName(args[1]);
					Player target = players.get(0);
					if (spell == null) {
						// Fail: no spell match
						sendMessage(this.strNoSpell, player, args);
					} else {
						Spellbook spellbook = MagicSpells.getSpellbook(player);
						if (spellbook == null || (!spellbook.hasSpell(spell) && this.requireKnownSpell)) {
							// Fail: player doesn't have spell
							sendMessage(this.strNoSpell, player, args);
						} else if (!spellbook.canTeach(spell)) {
							// Fail: cannot teach
							sendMessage(this.strCantTeach, player, args);
						} else {
							// Yay! can learn!
							Spellbook targetSpellbook = MagicSpells.getSpellbook(target);
							if (targetSpellbook == null || !targetSpellbook.canLearn(spell)) {
								// Fail: no spellbook for some reason or can't learn the spell
								sendMessage(this.strCantLearn, player, args);
							} else if (targetSpellbook.hasSpell(spell)) {
								// Fail: target already knows spell
								sendMessage(this.strAlreadyKnown, player, args);
							} else {
								// Call event
								boolean cancelled = callEvent(spell, target, player);
								if (cancelled) {
									// Fail: plugin cancelled it
									sendMessage(this.strCantLearn, player, args);
								} else {									
									targetSpellbook.addSpell(spell);
									targetSpellbook.save();
									sendMessage(formatMessage(this.strCastTarget, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", target.getDisplayName()), target, args);
									sendMessage(formatMessage(this.strCastSelf, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", target.getDisplayName()), player, args);
									playSpellEffects(EffectPosition.CASTER, player);
									playSpellEffects(EffectPosition.TARGET, target);
									return PostCastAction.NO_MESSAGES;
								}
							}
						}
					}
				}
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (args == null || args.length != 2) {
			// Fail: missing args
			sender.sendMessage(this.strUsage);
		} else {
			List<Player> players = MagicSpells.plugin.getServer().matchPlayer(args[0]);
			if (players.size() != 1) {
				// Fail: no player match
				sender.sendMessage(this.strNoTarget);
			} else {
				Spell spell = MagicSpells.getSpellByInGameName(args[1]);
				if (spell == null) {
					// Fail: no spell match
					sender.sendMessage(this.strNoSpell);
				} else {
					// Yay! can learn!
					Spellbook targetSpellbook = MagicSpells.getSpellbook(players.get(0));
					if (targetSpellbook == null || !targetSpellbook.canLearn(spell)) {
						// Fail: no spellbook for some reason or can't learn the spell
						sender.sendMessage(this.strCantLearn);
					} else if (targetSpellbook.hasSpell(spell)) {
						// Fail: target already knows spell
						sender.sendMessage(this.strAlreadyKnown);
					} else {
						// Call event
						boolean cancelled = callEvent(spell, players.get(0), sender);
						if (cancelled) {
							// Fail: cancelled by plugin
							sender.sendMessage(this.strCantLearn);
						} else {
							targetSpellbook.addSpell(spell);
							targetSpellbook.save();
							sendMessage(formatMessage(this.strCastTarget, "%a", getConsoleName(), "%s", spell.getName(), "%t", players.get(0).getDisplayName()), players.get(0), args);
							sender.sendMessage(formatMessage(this.strCastSelf, "%a", getConsoleName(), "%s", spell.getName(), "%t", players.get(0).getDisplayName()));
						}
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		String[] args = Util.splitParams(partial);
		if (args.length == 1) {
			// Matching player name
			return tabCompletePlayerName(sender, args[0]);
		}
		if (args.length == 2) {
			// Matching spell name
			return tabCompleteSpellName(sender, args[1]);
		}
		return null;
	}
	
	private boolean callEvent(Spell spell, Player learner, Object teacher) {
		SpellLearnEvent event = new SpellLearnEvent(spell, learner, LearnSource.TEACH, teacher);
		EventUtil.call(event);
		return event.isCancelled();
	}

}
