package com.nisovin.magicspells.util;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.targeted.DisguiseSpell;

public interface IDisguiseManager {
	
	void registerSpell(DisguiseSpell spell);
	void unregisterSpell(DisguiseSpell spell);
	int registeredSpellsCount();
	void addDisguise(Player player, DisguiseSpell.Disguise disguise);
	void removeDisguise(Player player);
	void removeDisguise(Player player, boolean sendPlayerPackets);
	void removeDisguise(Player player, boolean sendPlayerPackets, boolean delaySpawnPacket);
	boolean isDisguised(Player player);
	DisguiseSpell.Disguise getDisguise(Player player);
	void destroy();
	
}
