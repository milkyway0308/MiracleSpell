package com.nisovin.magicspells;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;

public class CastAtSubCommand {

	// Handles the /c castat command
	public static boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		// Begin /c castat handling
		
		if (args.length == 3 || args.length == 4) {
			// /c castat <spell> <player name> [power]
			Spell spell = MagicSpells.getSpellByInGameName(args[1]);
			Player target = Bukkit.getServer().getPlayer(args[2]);
			TargetedEntitySpell tes = null;
			TargetedLocationSpell tls = null;
			if (spell instanceof TargetedEntitySpell) {
				tes = (TargetedEntitySpell)spell;
			} else if (spell instanceof TargetedLocationSpell) {
				tls = (TargetedLocationSpell)spell;
			} else  {
				sender.sendMessage("You did not specify a targeted entity or targeted location spell");
				return true;
			}
			if (target == null) {
				sender.sendMessage("Could not find player:" + args[2]);
				return true;
			}
			float cPower = 1;
			if (args.length == 4) cPower = Float.parseFloat(args[3]);
			if (tes != null) {
				tes.castAtEntity(target, cPower);
			} else {
				tls.castAtLocation(target.getLocation(), cPower);
			}
			return true;
		}
		
		// End /c castat handling
		return true;
	}
	
}
