package com.nisovin.magicspells;

import org.bukkit.permissions.Permissible;

public enum Perm {
	
	SILENT("magicspells.silent"),
	NOREAGENTS("magicspells.noreagents"),
	NOCOOLDOWN("magicspells.nocooldown"),
	NOCASTTIME("magicspells.nocasttime"),
	NOTARGET("magicspells.notarget"),
	ADVANCEDSPELLBOOK("magicspells.advanced.spellbook"),
	ADVANCED_IMBUE("magicspells.advanced.imbue"),
	CAST("magicspells.cast."),
	LEARN("magicspells.learn."),
	GRANT("magicspells.grant."),
	TEMPGRANT("magicspells.tempgrant."),
	TEACH("magicspells.teach."),
	ADVANCED("magicspells.advanced."),
	ADVANCED_LIST("magicspells.advanced.list"),
	ADVANCED_FORGET("magicspells.advanced.forget"),
	ADVANCED_SCROLL("magicspells.advanced.scroll"),
	MODIFY_VARIABLE(null, true),
	MODIFY_MANA(null, true),
	SET_MAX_MANA(null, true),
	UPDATE_MANA_RANK(null, true),
	RESET_MANA(null, true),
	SET_MANA(null, true),
	MAGICITEM(null, true),
	DOWNLOAD(null, true),
	UPDATE(null, true),
	SAVESKIN(null, true),
	PROFILE(null, true),
	DEBUG(null, true),
	FORCECAST(null, true),
	RELOAD(null, true),
	RESET_COOLDOWN(null, true),
	CAST_AT(null, true),
	
	;
	
	private final boolean requireOp;
	public boolean requiresOp() { return this.requireOp; }
	private final boolean requireNode;
	public boolean requiresNode() { return this.requireNode; }
	private final String node;
	public String getNode() { return this.node; }
	public String getNode(Spell spell) { return this.node + spell.getPermissionName(); }
	
	public boolean has(Permissible permissible) {
		if (this.requiresOp() && !permissible.isOp()) return false;
		if (this.requiresNode() && !permissible.hasPermission(getNode())) return false;
		return true;
	}
	
	public boolean has(Permissible permissible, Spell spell) {
		if (this.requiresOp() && !permissible.isOp()) return false;
		if (this.requiresNode() && !permissible.hasPermission(getNode(spell))) return false;
		return true;
	}
	
	Perm(String node) {
		this(node, false);
	}
	
	Perm(String node, boolean requireOp) {
		this.node = node;
		this.requireNode = node != null;
		this.requireOp = requireOp;
	}
	
}
