package com.nisovin.magicspells.spells.command;

import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

// PLANNED OPTIONS
// get spellbook(s)
//     remove certain spells
//     remove all spells
//     remove bindings
// variables
//     remove certain variables
//     remove all variables
// MarkSpell
//     remove certain
//     remove all
// KeybindSpell
//     reset

// Spellbook spells
//     remove by player/world/all
public class ResetSpell extends CommandSpell {
	
	public ResetSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	// Arg format should be <player[,player[,player...]]>|all
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
		
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		return null;
	}
	
	
	
}
