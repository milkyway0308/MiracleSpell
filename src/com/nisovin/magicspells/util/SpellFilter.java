package com.nisovin.magicspells.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nisovin.magicspells.Spell;
import org.bukkit.configuration.ConfigurationSection;

public class SpellFilter {

	private Set<String> allowedSpells = null;
	private Set<String> blacklistedSpells = null;
	private Set<String> allowedTags = null;
	private Set<String> disallowedTags = null;
	
	private boolean defaultReturn;
	private boolean emptyFilter = false;
	
	public SpellFilter(List<String> allowedSpells, List<String> blacklistedSpells, List<String> allowedTags, List<String> disallowedTags) {
		
		// Initialize the collections
		if (allowedSpells != null && !allowedSpells.isEmpty()) {
			this.allowedSpells = new HashSet<>(allowedSpells);
		}
		if (blacklistedSpells != null && !blacklistedSpells.isEmpty()) {
			this.blacklistedSpells = new HashSet<>(blacklistedSpells);
		}
		if (allowedTags != null && !allowedTags.isEmpty()) {
			this.allowedTags = new HashSet<>(allowedTags);
		}
		if (disallowedTags != null && !disallowedTags.isEmpty()) {
			this.disallowedTags = new HashSet<>(disallowedTags);
		}
		
		// Determine the default outcome if nothing catches
		this.defaultReturn = determineDefaultValue();
	}
	
	private boolean determineDefaultValue() {
		// This means there is a tag whitelist check
		if (this.allowedTags != null) return false;
		
		// If there is a spell whitelist check
		if (this.allowedSpells != null) return false;
		
		// This means there is a tag blacklist
		if (this.disallowedTags != null) return true;
		
		// If there is a spell blacklist
		if (this.blacklistedSpells != null) return true;
		
		// If all of the collections are null, then there is no filter
		this.emptyFilter = true;
		return true;
	}
	
	public boolean check(Spell spell) {
		// Can't do anything if null anyway
		if (spell == null) return false;
		
		// Quick check to exit early if possible
		if (this.emptyFilter) return true;
		
		// Is it whitelisted explicitly?
		if (this.allowedSpells != null && this.allowedSpells.contains(spell.getInternalName())) return true;
		
		// Is it blacklisted?
		if (this.blacklistedSpells != null && this.blacklistedSpells.contains(spell.getInternalName())) return false;
		
		// Does it have a blacklisted tag?
		if (this.disallowedTags != null) {
			for (String tag: this.disallowedTags) {
				if (spell.hasTag(tag)) return false;
			}
		}
		
		// Does it have a whitelisted tag?
		if (this.allowedTags != null) {
			for (String tag: this.allowedTags) {
				if (spell.hasTag(tag)) return true;
			}
		}
		
		return this.defaultReturn;
	}
	
	public static SpellFilter fromConfig(MagicConfig config, String basePath) {
		basePath = basePath +  '.';
		List<String> spells = config.getStringList(basePath + "spells", null);
		List<String> deniedSpells = config.getStringList(basePath + "denied-spells", null);
		List<String> tagList = config.getStringList(basePath + "spell-tags", null);
		List<String> deniedTagList = config.getStringList(basePath + "denied-spell-tags", null);
		return new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
	}
	
	
}
