package com.nisovin.magicspells.spells.buff;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.MagicSpellsBlockBreakEvent;
import com.nisovin.magicspells.events.MagicSpellsBlockPlaceEvent;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;

// TODO this needs exemptions for anticheat
public class ReachSpell extends BuffSpell {
	
	private int range;
	private boolean consumeBlocks;
	private boolean dropBlocks;
	private Set<Material> disallowedBreakBlocks;
	private Set<Material> disallowedPlaceBlocks;
	
	private HashSet<String> reaching;
	
	public ReachSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		range = getConfigInt("range", 15);
		consumeBlocks = getConfigBoolean("consume-blocks", true);
		dropBlocks = getConfigBoolean("drop-blocks", true);
		
		disallowedBreakBlocks = EnumSet.noneOf(Material.class);
		List<String> list = getConfigStringList("disallowed-break-blocks", null);
		if (list != null) {
			for (String s : list) {
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
				if (m == null) continue;
				Material material = m.getMaterial();
				if (material == null) continue;
				disallowedBreakBlocks.add(material);
			}
		}
		
		disallowedPlaceBlocks = EnumSet.noneOf(Material.class);
		list = getConfigStringList("disallowed-place-blocks", null);
		if (list != null) {
			for (String s : list) {
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
				if (m == null) continue;
				Material material = m.getMaterial();
				if (material == null) continue;
				disallowedPlaceBlocks.add(material);
			}
		}
		
		reaching = new HashSet<>();
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		reaching.add(player.getName());
		return true;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!isActive(event.getPlayer())) return;
		Player player = event.getPlayer();
		
		// Check expired
		if (isExpired(player)) {
			turnOff(player);
			return;
		}
		
		// Get targeted block
		Action action = event.getAction();
		List<Block> targets = getLastTwoTargetedBlocks(player, range);
		Block airBlock;
		Block targetBlock;
		if (targets == null) return;
		if (targets.size() != 2) return;
		airBlock = targets.get(0);
		targetBlock = targets.get(1);
		if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && targetBlock.getType() != Material.AIR) {
			// Break
			
			// Check for disallowed
			if (disallowedBreakBlocks.contains(targetBlock.getType())) return;
			
			// Call break event
			MagicSpellsBlockBreakEvent evt = new MagicSpellsBlockBreakEvent(targetBlock, player);
			EventUtil.call(evt);
			if (!evt.isCancelled()) {
				// Remove block
				targetBlock.getWorld().playEffect(targetBlock.getLocation(), Effect.STEP_SOUND, targetBlock.getType());
				// Drop item
				if (dropBlocks && player.getGameMode() == GameMode.SURVIVAL) {
					targetBlock.breakNaturally();
				} else {
					targetBlock.setType(Material.AIR);
				}
				addUseAndChargeCost(player);
			}
		} else if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && targetBlock.getType() != Material.AIR) {
			// Place
			
			// Check for block in hand
			ItemStack inHand = HandHandler.getItemInMainHand(player);
			if (inHand != null && inHand.getType() != Material.AIR && inHand.getType().isBlock()) {
				
				// Check for disallowed
				if (disallowedPlaceBlocks.contains(inHand.getType())) return;
				
				BlockState prevState = airBlock.getState();
				
				// Place block
				BlockState state = airBlock.getState();
				state.setType(inHand.getType());
				state.setData(inHand.getData());
				state.update(true);
				
				// Call event
				MagicSpellsBlockPlaceEvent evt = new MagicSpellsBlockPlaceEvent(airBlock, prevState, targetBlock, inHand, player, true);
				EventUtil.call(evt);
				if (evt.isCancelled()) {
					// Cancelled, revert
					prevState.update(true);
				} else {
					// Remove item from hand
					if (consumeBlocks && player.getGameMode() != GameMode.CREATIVE) {
						if (inHand.getAmount() > 1) {
							inHand.setAmount(inHand.getAmount() - 1);
							HandHandler.setItemInMainHand(player, inHand);
						} else {
							HandHandler.setItemInMainHand(player, null);
						}
					}
					addUseAndChargeCost(player);
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void turnOffBuff(Player player) {
		reaching.remove(player.getName());
	}
	
	@Override
	protected void turnOff() {
		reaching.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return reaching.contains(player.getName());
	}

}
