package com.nisovin.magicspells.spells.command;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellUtil;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// NOTE: THIS DOES NOT PERFORM MANY SAFETY CHECKS, IT IS MEANT TO BE FAST FOR ADMINS
// NOTE: THIS CURRENTLY ONLY CASTS FROM CONSOLE
public class AdminTeachSpell extends CommandSpell {
	
	public AdminTeachSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		// FIXME add more descriptive messages
		// Need at least name and one node
		if (args.length < 2) return false;
		Player targetPlayer = Bukkit.getPlayer(args[0]);
		if (targetPlayer == null) return false;
		
		Spellbook spellbook = MagicSpells.getSpellbook(targetPlayer);
		if (spellbook == null) return false;
		// No target, TODO add messages
		
		
		String[] nodes = Arrays.copyOfRange(args, 1, args.length);
		Set<String> nodeSet = new HashSet<>(Arrays.asList(nodes));
		
		new AdminTeachTask(sender, spellbook, nodeSet).runTaskAsynchronously(MagicSpells.plugin);
		
		// Format should be <target> <node> <node> <...>
		return true;
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		return null;
	}
	
	private static class AdminTeachTask extends BukkitRunnable {
		
		private final CommandSender sender;
		private final Spellbook spellbook;
		private final Set<String> nodeSet;
		
		public AdminTeachTask(CommandSender sender, Spellbook spellbook, Set<String> nodeSet) {
			this.sender = sender;
			this.spellbook = spellbook;
			this.nodeSet = nodeSet;
		}
		
		@Override
		public void run() {
			// TODO can the retrieval of Magicspells::spells be done async or does that need to be done sync?
			final Collection<Spell> spellCollection = SpellUtil.getSpellsByPermissionNames(MagicSpells.spells(), this.nodeSet);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, () -> {
				if (this.spellbook.getSpells() == null) {
					sender.sendMessage("Target spellbook was destroyed before changes could be applied.");
					return;
				}
				Util.forEachOrdered(spellCollection, this.spellbook::addSpell);
				this.spellbook.save();
				sender.sendMessage("Spell granting complete");
			});
			
		}
		
	}
	
}
