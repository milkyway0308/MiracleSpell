package com.nisovin.magicspells.spells.buff;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class StealthSpell extends BuffSpell {
	
	private HashSet<String> stealthy;
	
	public StealthSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.stealthy = new HashSet<>();
	}
	
	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		this.stealthy.add(player.getName());
		return true;
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;
		if (this.stealthy.isEmpty()) return;
		if (!(event.getTarget() instanceof Player)) return;
		Player player = (Player)event.getTarget();
		if (!this.stealthy.contains(player.getName())) return;
		if (isExpired(player)) {
			turnOff(player);
		} else {
			addUse(player);
			boolean ok = chargeUseCost(player);
			if (ok) event.setCancelled(true);
		}
	}
	
	@Override
	public void turnOffBuff(Player player) {
		this.stealthy.remove(player.getName());
	}
	
	@Override
	protected void turnOff() {
		this.stealthy.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return this.stealthy.contains(player.getName());
	}
	
}
