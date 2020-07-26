package com.nisovin.magicspells.volatilecode;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.targeted.DisguiseSpell.Disguise;
import com.nisovin.magicspells.util.DisguiseManager;
import com.nisovin.magicspells.util.MagicConfig;

public class DisguiseManagerEmpty extends DisguiseManager {

	public DisguiseManagerEmpty(MagicConfig config) {
		super(config);
	}

	@Override
	protected void cleanup() {
		// No op
	}

	@Override
	protected void sendDestroyEntityPackets(Player disguised) {
		// No op
	}

	@Override
	protected void sendDestroyEntityPackets(Player disguised, int entityId) {
		// No op
	}

	@Override
	protected void sendDisguisedSpawnPackets(Player disguised, Disguise disguise) {
		// No op
	}

	@Override
	protected void sendPlayerSpawnPackets(Player player) {
		// No op
	}

}
