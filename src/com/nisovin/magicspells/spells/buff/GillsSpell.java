package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

public class GillsSpell extends BuffSpell {

	@ConfigData(field="glass-head-effect", dataType="boolean", defaultValue="true")
	private boolean glassHeadEffect;
	
	private HashSet<String> fishes;
	private HashMap<Player,ItemStack> helmets;
	
	public GillsSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		glassHeadEffect = getConfigBoolean("glass-head-effect", true);
		
		fishes = new HashSet<>();
		if (glassHeadEffect) helmets = new HashMap<>();
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		fishes.add(player.getName());
		PlayerInventory inventory = player.getInventory();
		if (glassHeadEffect) {
			ItemStack helmet = inventory.getHelmet();
			if (helmet != null && helmet.getType() != Material.AIR) helmets.put(player, helmet);
			inventory.setHelmet(new ItemStack(Material.GLASS, 1));
		}
		return true;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		if (event.getCause() != DamageCause.DROWNING) return;
		
		Player player = (Player)entity;
		
		if (!fishes.contains(player.getName())) return;
		if (isExpired(player)) {
			turnOff(player);
		} else {
			addUse(player);
			boolean ok = chargeUseCost(player);
			if (ok) {
				event.setCancelled(true);
				player.setRemainingAir(player.getMaximumAir());
			}
		}
	}

	@Override
	public void turnOffBuff(Player player) {
		if (!fishes.remove(player.getName())) return;
		if (!glassHeadEffect) return;
		
		boolean playerOnline = player.isOnline();
		PlayerInventory inventory = player.getInventory();
		ItemStack helmet = inventory.getHelmet();
		
		if (helmets.containsKey(player)) {
			if (playerOnline) inventory.setHelmet(helmets.get(player));
			helmets.remove(player);
		} else if (helmet != null && helmet.getType() == Material.GLASS) {
			if (playerOnline) inventory.setHelmet(null);				
		}
	}
	
	@Override
	protected void turnOff() {
		if (glassHeadEffect) {
			for (String name : fishes) {
				Player player = PlayerNameUtils.getPlayerExact(name);
				if (player == null) continue;
				if (!player.isOnline()) continue;
				
				PlayerInventory inventory = player.getInventory();
				
				if (helmets.containsKey(player)) {
					inventory.setHelmet(helmets.get(player));
					continue;
				}
				
				ItemStack helmet = inventory.getHelmet();
				if (helmet == null) continue;
				if (helmet.getType() != Material.GLASS) continue;
				
				inventory.setHelmet(null);
			}
		}
		if (helmets != null) helmets.clear();
		fishes.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return fishes.contains(player.getName());
	}

}
