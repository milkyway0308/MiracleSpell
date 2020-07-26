package com.nisovin.magicspells.spells.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

// Advanced perm is for listing other player's spells
public class SublistSpell extends CommandSpell {
	
	private int lineLength = 60;
	
	@ConfigData(field="only-show-castable-spells", dataType="boolean", defaultValue="false")
	private boolean onlyShowCastableSpells;
	
	@ConfigData(field="reload-granted-spells", defaultValue="true")
	private boolean reloadGrantedSpells;
	
	@ConfigData(field="spells-to-hide", dataType="String[]", defaultValue="null")
	private List<String> spellsToHide;
	
	@ConfigData(field="spells-to-show", dataType="String[]", defaultValue="null")
	private List<String> spellsToShow;
	
	@ConfigData(field="str-no-spells", dataType="String", defaultValue="You do not know any spells.")
	private String strNoSpells;
	
	@ConfigData(field="str-prefix", dataType="String", defaultValue="Known spells:")
	private String strPrefix;

	public SublistSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.onlyShowCastableSpells = getConfigBoolean("only-show-castable-spells", false);
		this.reloadGrantedSpells = getConfigBoolean("reload-granted-spells", true);
		this.spellsToHide = getConfigStringList("spells-to-hide", null);
		this.spellsToShow = getConfigStringList("spells-to-show", null);
		this.strNoSpells = getConfigString("str-no-spells", "You do not know any spells.");
		this.strPrefix = getConfigString("str-prefix", "Known spells:");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			String extra = "";
			if (args != null && args.length > 0 && spellbook.hasAdvancedPerm("list")) {
				Player p = PlayerNameUtils.getPlayer(args[0]);
				if (p != null) {
					spellbook = MagicSpells.getSpellbook(p);
					extra = '(' + p.getDisplayName() + ") ";
				}
			}
			if (spellbook != null && this.reloadGrantedSpells) {
				spellbook.addGrantedSpells();
			}
			if (spellbook == null || spellbook.getSpells().isEmpty()) {
				// No spells
				sendMessage(this.strNoSpells, player, args);
			} else {
				String s = "";
				for (Spell spell : spellbook.getSpells()) {
					if (!spell.isHelperSpell() && (!this.onlyShowCastableSpells || spellbook.canCast(spell)) && !(this.spellsToHide != null && this.spellsToHide.contains(spell.getInternalName())) && (this.spellsToShow == null || this.spellsToShow.contains(spell.getInternalName()))) {
						if (s.isEmpty()) {
							s = spell.getName();
						} else {
							s += ", " + spell.getName();
						}
					}
				}
				s = this.strPrefix + ' ' + extra + s;
				while (s.length() > this.lineLength) {
					int i = s.substring(0, this.lineLength).lastIndexOf(' ');
					sendMessage(s.substring(0, i), player, args);
					s = s.substring(i + 1);
				}
				if (!s.isEmpty()) {
					sendMessage(s, player, args);
				}
			}
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		if (sender instanceof ConsoleCommandSender) {
			if (!partial.contains(" ")) return tabCompletePlayerName(sender, partial);
		}
		return null;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		StringBuilder s = new StringBuilder();
		
		// Get spell list
		Collection<Spell> spells = MagicSpells.spells();
		if (args != null && args.length > 0) {
			Player p = PlayerNameUtils.getPlayer(args[0]);
			if (p == null) {
				sender.sendMessage("No such player.");
				return true;
			} else {
				spells = MagicSpells.getSpellbook(p).getSpells();
				s.append(p.getName() + "'s spells: ");
			}
		} else {
			s.append("All spells: ");
		}
		
		// Create string of spells
		for (Spell spell : spells) {
			s.append(spell.getName());
			s.append(' ');
		}
		
		// Send message
		sender.sendMessage(s.toString());
		
		return true;
	}

}
