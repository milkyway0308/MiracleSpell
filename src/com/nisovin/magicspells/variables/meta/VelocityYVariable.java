package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.MetaVariable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityYVariable extends MetaVariable {
	
	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getVelocity().getY();
		return 0D;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			Vector velocity = p.getVelocity();
			velocity.setY(amount);
			p.setVelocity(velocity);
		}
	}
	
	@Override
	public boolean modify(String player, double amount) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) {
			Vector velocity = p.getVelocity();
			velocity.setY(velocity.getY() + amount);
			p.setVelocity(velocity);
			return true;
		}
		return false;
	}
	
}
