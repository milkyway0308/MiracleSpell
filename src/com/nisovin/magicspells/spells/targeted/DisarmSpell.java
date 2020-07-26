package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.Util;

public class DisarmSpell extends TargetedSpell implements TargetedEntitySpell {

	private Set<Material> disarmable;
	private int disarmDuration;
	private boolean dontDrop;
	private boolean preventTheft;
	private String strInvalidItem;
	
	private HashMap<Item, String> disarmedItems;
	
	public DisarmSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		List<String> disarmableIds = getConfigStringList("disarmable-items", null);
		if (disarmableIds != null && !disarmableIds.isEmpty()) {
			disarmable = new HashSet<>();
			for (String itemName : disarmableIds) {
				ItemStack item = Util.getItemStackFromString(itemName);
				if (item != null) disarmable.add(item.getType());
			}
		}
		
		disarmDuration = getConfigInt("disarm-duration", 100);
		dontDrop = getConfigBoolean("dont-drop", false);
		preventTheft = getConfigBoolean("prevent-theft", true);
		strInvalidItem = getConfigString("str-invalid-item", "Your target could not be disarmed.");
		
		if (dontDrop) preventTheft = false;
		if (preventTheft) disarmedItems = new HashMap<>();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// Get target
			TargetInfo<LivingEntity> target = getTargetedEntity(player, power);
			if (target == null) {
				// Fail
				return noTarget(player);
			}
			
			LivingEntity realTarget = target.getTarget();
			
			boolean disarmed = disarm(realTarget);
			if (disarmed) {
				playSpellEffects(player, realTarget);
				// Send messages
				sendMessages(player, realTarget);
				return PostCastAction.NO_MESSAGES;
			}
			
			// Fail
			return noTarget(player, strInvalidItem);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean disarm(final LivingEntity target) {
		final ItemStack inHand = getItemInHand(target);
		if (disarmable == null || disarmable.contains(inHand.getType())) {
			if (dontDrop) {
				// Hide item
				setItemInHand(target, null);
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					@Override
					public void run() {
						// Give the item back
						ItemStack inHand2 = getItemInHand(target);
						if (inHand2 == null || inHand2.getType() == Material.AIR) {
							// Put back in hand
							setItemInHand(target, inHand);
						} else if (target instanceof Player) {
							// Hand is full
							int slot = ((Player)target).getInventory().firstEmpty();
							if (slot >= 0) {
								// Put in first available slot
								((Player)target).getInventory().setItem(slot, inHand);
							} else {
								// No slots available, drop at feet
								Item item = target.getWorld().dropItem(target.getLocation(), inHand);
								item.setPickupDelay(0);
							}
						}
					}
				}, disarmDuration);
			} else {
				// Drop item
				setItemInHand(target, null);
				Item item = target.getWorld().dropItemNaturally(target.getLocation(), inHand.clone());
				item.setPickupDelay(disarmDuration);
				if (preventTheft && target instanceof Player) disarmedItems.put(item, target.getName());
			}
			return true;
		}
		return false;
	}
	
	ItemStack getItemInHand(LivingEntity entity) {
		EntityEquipment equip = entity.getEquipment();
		if (equip == null) return null;
		return HandHandler.getItemInMainHand(equip);
	}
	
	void setItemInHand(LivingEntity entity, ItemStack item) {
		EntityEquipment equip = entity.getEquipment();
		if (equip == null) return;
		HandHandler.setItemInMainHand(equip, item);
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) return false;
		boolean disarmed =  disarm(target);
		if (disarmed) playSpellEffects(caster, target);
		return disarmed;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!validTargetList.canTarget(target)) return false;
		boolean disarmed = disarm(target);
		if (disarmed) playSpellEffects(EffectPosition.TARGET, target);
		return disarmed;
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (!preventTheft || event.isCancelled()) return;
		
		Item item = event.getItem();
		if (!disarmedItems.containsKey(item)) return;
		if (disarmedItems.get(item).equals(event.getPlayer().getName())) {
			disarmedItems.remove(item);
		} else {
			event.setCancelled(true);
		}
	}

}
