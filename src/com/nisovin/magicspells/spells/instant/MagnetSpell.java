package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.util.MagicConfig;

public class MagnetSpell extends InstantSpell implements TargetedLocationSpell {
	
	private double range;	
	private double velocity;
	
	private boolean teleport;
	private boolean forcepickup;
	private boolean removeItemGravity;
	
	public MagnetSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.range = getConfigDouble("range", 5D);
		this.velocity = getConfigDouble("velocity", 1D);
		this.teleport = getConfigBoolean("teleport-items", false);
		this.forcepickup = getConfigBoolean("force-pickup", false);
		this.removeItemGravity = getConfigBoolean("remove-item-gravity", false);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			
			double radius = this.range * power;			
			List<Item> items = getNearbyItems(player, radius);
			
			magnet(player, items, power);
		}
		playSpellEffects(EffectPosition.CASTER, player);
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void magnet(Player player, Collection<Item> items, float power) {
		Location loc = player.getLocation();
		for (Item i: items) {
			magnet(loc, i, power);
		}
	}
		
	private void magnet(Location origin, Item item, float power) {
		
		// Handle gravity removal
		if (this.removeItemGravity) {
			MagicSpells.getVolatileCodeHandler().setGravity(item, false);
		}
		
		// Handle item entity movement
		if (this.teleport) {
			item.teleport(origin);	
		} else {
			item.setVelocity(origin.toVector().subtract(item.getLocation().toVector()).normalize().multiply(this.velocity * power));
		}
		
		playSpellEffects(EffectPosition.PROJECTILE, item);
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		// Can't have a null caster
		if (caster == null) return false;
		
		// Get the items nearby
		Collection<Item> targetItems = getNearbyItems(target, this.range * power);
		
		// Magnet them
		magnet(caster, targetItems, power);
		
		return true;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		return false;
	}
	
	private List<Item> getNearbyItems(Location center, double radius) {
		ItemStack i = new ItemStack(Material.STONE);
		Item e = center.getWorld().dropItem(center, i);
		e.setPickupDelay(1000);
		
		List<Item> ret = getNearbyItems(e, radius);
		
		e.remove();
		
		return ret;
	}
	
	private List<Item> getNearbyItems(Entity center, double radius) {
		List<Entity> entities = center.getNearbyEntities(radius, radius, radius);
		
		List<Item> ret = new ArrayList<>();
		for (Entity e: entities) {
			if (!(e instanceof Item)) continue;;
			Item i = (Item)e;
			ItemStack stack = i.getItemStack();
			if (!InventoryUtil.isNothing(stack) && !i.isDead()) {
				if (this.forcepickup) {
					i.setPickupDelay(0);
					ret.add(i);
				} else if (i.getPickupDelay() < i.getTicksLived()){
					ret.add(i);
				}
			}
		}
		if (center instanceof Item) ret.remove(center);
		return ret;
	}
	
}
