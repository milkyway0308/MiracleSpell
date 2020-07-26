package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.HashSet;

import com.nisovin.magicspells.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.BlockPlatform;
import com.nisovin.magicspells.util.LocationUtil;
import com.nisovin.magicspells.util.MagicConfig;

public class CarpetSpell extends BuffSpell {
	
	Material platformBlock;
	private int size;
	private boolean cancelOnTeleport;
	
	HashMap<String,BlockPlatform> windwalkers;
	HashSet<Player> falling;
	private Listener listener;

	public CarpetSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(getConfigString("platform-block", "glass"));
		if (m != null) {
			platformBlock = m.getMaterial();
		} else {
			platformBlock = Material.GLASS;
		}
		size = getConfigInt("size", 2);
		cancelOnTeleport = getConfigBoolean("cancel-on-teleport", true);
		
		windwalkers = new HashMap<>();
		falling = new HashSet<>();
	}

	@Override
	public void initialize() {
		super.initialize();
		if (cancelOnLogout) registerEvents(new QuitListener());
		if (cancelOnTeleport) registerEvents(new TeleportListener());
	}
	
	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		windwalkers.put(player.getName(), new BlockPlatform(platformBlock, Material.AIR, player.getLocation().getBlock().getRelative(0, -1, 0), size, true, "square"));
		registerListener();
		return true;
	}
	
	private void registerListener() {
		if (listener != null) return;
		
		listener = new CarpetListener();
		registerEvents(listener);
	}
	
	private void unregisterListener() {
		if (listener == null) return;
		if (!windwalkers.isEmpty()) return;
		
		unregisterEvents(listener);
		listener = null;
	}

	public class CarpetListener implements Listener {
	
		@EventHandler(priority=EventPriority.MONITOR)
		public void onPlayerMove(PlayerMoveEvent event) {
			Player player = event.getPlayer();
			BlockPlatform platform = windwalkers.get(player.getName());
			if (platform == null) return;
			
			if (isExpired(player)) {
				turnOff(player);
			} else {
				if (falling.contains(player)) {
					if (event.getTo().getY() < event.getFrom().getY()) {
						falling.remove(player);
					} else {
						return;
					}
				}
				if (!player.isSneaking()) { 
					Block block = event.getTo().subtract(0, 1, 0).getBlock();
					boolean moved = platform.isMoved(block, false);
					if (moved) {
						platform.movePlatform(block, true);
						addUse(player);
						chargeUseCost(player);
					}
				}
			}
		}
	
		@EventHandler(priority=EventPriority.MONITOR)
		public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
			Player player = event.getPlayer();
			String playerName = player.getName();
			
			if (!windwalkers.containsKey(playerName)) return;
			if (!event.isSneaking()) return;
			
			if (isExpired(player)) {
				turnOff(player);
				return;
			}
			Block block = player.getLocation().subtract(0, 2, 0).getBlock();
			boolean moved = windwalkers.get(playerName).movePlatform(block);
			if (moved) {
				falling.add(player);
				addUse(player);
				chargeUseCost(player);
			}
		}
	
		@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
		public void onBlockBreak(BlockBreakEvent event) {
			if (windwalkers.isEmpty()) return;
			final Block block = event.getBlock();
			if (block.getType() != platformBlock) return;
			if (Util.containsValueParallel(windwalkers, platform -> platform.blockInPlatform(block))) event.setCancelled(true);
		}
		
	}

	public class TeleportListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			Player player = event.getPlayer();
			if (!windwalkers.containsKey(player.getName())) return;
			
			Location locationFrom = event.getFrom();
			Location locationTo = event.getTo();
			if (LocationUtil.differentWorldDistanceGreaterThan(locationFrom, locationTo, 50)) {
				turnOff(player);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerPortal(PlayerPortalEvent event) {
			Player player = event.getPlayer();
			if (!windwalkers.containsKey(player.getName())) return;
			turnOff(player);
		}
		
	}
	
	@Override
	public void turnOffBuff(Player player) {
		String playerName = player.getName();
		BlockPlatform platform = windwalkers.get(playerName);
		if (platform == null) return;
		
		platform.destroyPlatform();
		windwalkers.remove(playerName);
		unregisterListener();
	}
	
	@Override
	protected void turnOff() {
		Util.forEachValueOrdered(windwalkers, BlockPlatform::destroyPlatform);
		windwalkers.clear();
		unregisterListener();
	}

	@Override
	public boolean isActive(Player player) {
		return windwalkers.containsKey(player.getName());
	}

}
