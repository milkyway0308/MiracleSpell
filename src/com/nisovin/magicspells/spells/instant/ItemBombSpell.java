package com.nisovin.magicspells.spells.instant;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class ItemBombSpell extends InstantSpell implements TargetedLocationSpell {

	float velocity;
	float verticalAdjustment;
	int rotationOffset;
	float yOffset;
	ItemStack item;
	String itemName;
	int itemNameDelay;
	int delay;
	Subspell spell;
	boolean itemHasGravity;
	
	public ItemBombSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.velocity = getConfigFloat("velocity", 1);
		this.verticalAdjustment = getConfigFloat("vertical-adjustment", 0.5F);
		this.rotationOffset = getConfigInt("rotation-offset", 0);
		this.yOffset = getConfigFloat("y-offset", 1F);
		this.item = Util.getItemStackFromString(getConfigString("item", "stone"));
		this.itemName = getConfigString("item-name", null);
		this.itemNameDelay = getConfigInt("item-name-delay", 1);
		this.delay = getConfigInt("delay", 100);
		this.itemHasGravity = getConfigBoolean("gravity", true);
		this.spell = new Subspell(getConfigString("spell", ""));
		
		if (this.item == null) MagicSpells.error("Invalid item on ItemBombSpell " + this.internalName);
		if (this.itemName != null) this.itemName = ChatColor.translateAlternateColorCodes('&', this.itemName);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (!this.spell.process()) {
			MagicSpells.error("Invalid spell on ItemBombSpell " + this.internalName);
		}
	}

	@Override
	public PostCastAction castSpell(final Player player, SpellCastState state, final float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location l = player.getLocation().add(0, this.yOffset, 0);
			spawnItem(player, l, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void spawnItem(final Player player, Location l, final float power) {
		Vector v = getVector(l, power);
		final Item i = l.getWorld().dropItem(l, this.item);
		i.teleport(l);
		i.setVelocity(v);
		MagicSpells.getVolatileCodeHandler().setGravity(i, this.itemHasGravity);
		i.setPickupDelay(this.delay << 1);
		if (this.itemName != null) {
			MagicSpells.scheduleDelayedTask(() -> { i.setCustomName(itemName); i.setCustomNameVisible(true); }, this.itemNameDelay);
		}
		MagicSpells.scheduleDelayedTask(new Runnable() {
			@Override
			public void run() {
				Location l = i.getLocation();
				i.remove();
				playSpellEffects(EffectPosition.TARGET, l);
				spell.castAtLocation(player, l, power);
			}
		}, this.delay);
		
		if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player);
		} else {
			playSpellEffects(EffectPosition.CASTER, l);
		}
	}
	
	private Vector getVector(Location loc, float power) {
		Vector v = loc.getDirection();
		if (this.verticalAdjustment != 0) v.setY(v.getY() + this.verticalAdjustment);
		if (this.rotationOffset != 0) Util.rotateVector(v, this.rotationOffset);
		v.normalize().multiply(this.velocity);
		return v;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		spawnItem(caster, target, power);
		return true;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		spawnItem(null, target, power);
		return true;
	}

}
