package com.nisovin.magicspells.variables.meta;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;

public class CoordPitchVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getLocation().getPitch();
		return 0D;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			Location to = p.getLocation();
			to.setPitch((float)amount);
			p.teleport(to, TeleportCause.PLUGIN);
		}
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			Location to = p.getLocation();
			to.setPitch((float) (to.getPitch() + amount));
			p.teleport(to, TeleportCause.PLUGIN);
			return true;
		}
		return false;
	}

}
