package com.nisovin.magicspells.spells.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellForgetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.util.Util;

// Advanced perm allows you to make others forget a spell
// Put * for the spell to forget all of them
public class ForgetSpell extends CommandSpell {

	private boolean allowSelfForget;
	private String strUsage;
	private String strNoTarget;
	private String strNoSpell;
	private String strDoesntKnow;
	private String strCastTarget;
	private String strCastSelfTarget;
	private String strResetTarget;
	private String strResetSelf;
	
	public ForgetSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.allowSelfForget = getConfigBoolean("allow-self-forget", true);
		this.strUsage = getConfigString("str-usage", "Usage: /cast forget <target> <spell>");
		this.strNoTarget = getConfigString("str-no-target", "No such player.");
		this.strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		this.strDoesntKnow = getConfigString("str-doesnt-know", "That person does not know that spell.");
		this.strCastTarget = getConfigString("str-cast-target", "%a has made you forget the %s spell.");
		this.strCastSelfTarget = getConfigString("str-cast-self-target", "You have forgotten the %s spell.");
		this.strResetTarget = getConfigString("str-reset-target", "You have reset %t's spellbook.");
		this.strResetSelf = getConfigString("str-reset-self", "You have forgotten all of your spells.");
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0 || args.length > 2) {
				// Fail: missing args
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
				// Get caster spellbook
			Spellbook casterSpellbook = MagicSpells.getSpellbook(player);
			
			// Get target
			Player target;
			if (args.length == 1 && this.allowSelfForget) {
				target = player;
			} else if (args.length == 2 && casterSpellbook.hasAdvancedPerm("forget")) {
				List<Player> players = MagicSpells.plugin.getServer().matchPlayer(args[0]);
				if (players.size() != 1) {
					// Fail: no player match
					sendMessage(this.strNoTarget, player, args);
					return PostCastAction.ALREADY_HANDLED;
				}
				target = players.get(0);
			} else {
				// Fail: missing args
				sendMessage(this.strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get spell
			String spellName = args.length == 1 ? args[0] : args[1];
			boolean all = false;
			Spell spell = null;
			if (spellName.equals("*")) {
				all = true;
			} else {
				spell = MagicSpells.getSpellByInGameName(spellName);
			}
			if (spell == null && !all) {
				// Fail: no spell match
				sendMessage(this.strNoSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Check if caster has spell
			if (!all && !casterSpellbook.hasSpell(spell)) {
				// Fail: caster doesn't have spell
				sendMessage(strNoSpell, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get target spellbook and perform checks
			Spellbook targetSpellbook = MagicSpells.getSpellbook(target);
			if (targetSpellbook == null || (!all && !targetSpellbook.hasSpell(spell))) {
				// Fail: error
				sendMessage(this.strDoesntKnow, player, args);
				return PostCastAction.ALREADY_HANDLED;
			} 
			
			// Remove spell(s)
			if (!all) {
				targetSpellbook.removeSpell(spell);
				targetSpellbook.save();
				if (!player.equals(target)) {
					sendMessage(formatMessage(this.strCastTarget, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", target.getDisplayName()), target, args); //TODO check for null access
					sendMessage(formatMessage(this.strCastSelf, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", target.getDisplayName()), player, args);
					playSpellEffects(EffectPosition.CASTER, player);
					playSpellEffects(EffectPosition.TARGET, target);
				} else {
					sendMessage(formatMessage(this.strCastSelfTarget, "%s", spell.getName()), player, args); //TODO check for null access
					playSpellEffects(EffectPosition.CASTER, player);
				}
				return PostCastAction.NO_MESSAGES;
			}
			targetSpellbook.removeAllSpells();
			targetSpellbook.addGrantedSpells();
			targetSpellbook.save();
			if (!player.equals(target)) {
				sendMessage(formatMessage(this.strResetTarget, "%t", target.getDisplayName()), player, args);
				playSpellEffects(EffectPosition.CASTER, player);
				playSpellEffects(EffectPosition.TARGET, target);
			} else {
				sendMessage(this.strResetSelf, player, args);
				playSpellEffects(EffectPosition.CASTER, player);
			}
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (args == null || args.length != 2) {
			// fail: missing args
			sender.sendMessage(this.strUsage);
		} else {
			Player target = PlayerNameUtils.getPlayer(args[0]);
			if (target == null) {
				// fail: no player match
				sender.sendMessage(this.strNoTarget);
			} else {
				Spell spell = null;
				boolean all = false;
				if (args[1].equals("*")) {
					all = true;
				} else {
					spell = MagicSpells.getSpellByInGameName(args[1]);
				}
				if (spell == null && !all) {
					// fail: no spell match
					sender.sendMessage(this.strNoSpell);
				} else {
					Spellbook targetSpellbook = MagicSpells.getSpellbook(target);
					if (targetSpellbook == null || (!all && !targetSpellbook.hasSpell(spell))) {
						// Fail: no spellbook for some reason or can't learn the spell
						sender.sendMessage(this.strDoesntKnow);
					} else {
						SpellForgetEvent forgetEvent = new SpellForgetEvent(spell, target);
						EventUtil.call(forgetEvent);
						if (!forgetEvent.isCancelled()) {
							if (!all) {
								targetSpellbook.removeSpell(spell);
								targetSpellbook.save();
								sendMessage(formatMessage(this.strCastTarget, "%a", getConsoleName(), "%s", spell.getName(), "%t", target.getDisplayName()), target, args); //TODO check for null access
								sender.sendMessage(formatMessage(this.strCastSelf, "%a", getConsoleName(), "%s", spell.getName(), "%t", target.getDisplayName()));
							} else {
								targetSpellbook.removeAllSpells();
								targetSpellbook.addGrantedSpells();
								targetSpellbook.save();
								sender.sendMessage(formatMessage(this.strResetTarget, "%t", target.getDisplayName()));
							}
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
			// Matching player name or spell name
			List<String> options = new ArrayList<>();
			List<String> players = tabCompletePlayerName(sender, args[0]);
			List<String> spells = tabCompleteSpellName(sender, args[0]);
			if (players != null) options.addAll(players);
			if (spells != null) options.addAll(spells);
			if (!options.isEmpty()) return options;
		}
		
		if (args.length == 2) {
			// Matching spell name
			return tabCompleteSpellName(sender, args[1]);
		}
		
		return null;
	}

}
