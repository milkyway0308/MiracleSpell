package com.nisovin.magicspells.spells.instant;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class ItemProjectileSpell extends InstantSpell {

	float speed;
	boolean vertSpeedUsed;
	float hitRadius;
	float vertSpeed;
	float yOffset;
	boolean projectileHasGravity;
	ItemStack item;
	Subspell spellOnHitEntity;
	Subspell spellOnHitGround;
	
	public ItemProjectileSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.speed = getConfigFloat("speed", 1);
		this.vertSpeedUsed = configKeyExists("vert-speed");
		this.vertSpeed = getConfigFloat("vert-speed", 0);
		this.hitRadius = getConfigFloat("hit-radius", 1);
		this.yOffset = getConfigFloat("y-offset", 0);
		this.projectileHasGravity = getConfigBoolean("gravity", true);
		
		if (configKeyExists("spell-on-hit-entity")) this.spellOnHitEntity = new Subspell(getConfigString("spell-on-hit-entity", ""));
		if (configKeyExists("spell-on-hit-ground")) this.spellOnHitGround = new Subspell(getConfigString("spell-on-hit-ground", ""));
		
		this.item = Util.getItemStackFromString(getConfigString("item", "iron_sword"));
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if (this.spellOnHitEntity != null && !this.spellOnHitEntity.process()) {
			this.spellOnHitEntity = null;
			MagicSpells.error("Invalid spell-on-hit-entity for " + this.internalName);
		}
		if (this.spellOnHitGround != null && !this.spellOnHitGround.process()) {
			this.spellOnHitGround = null;
			MagicSpells.error("Invalid spell-on-hit-ground for " + this.internalName);
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			new ItemProjectile(player, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	class ItemProjectile implements Runnable {
		
		Player caster;
		float power;
		Item entity;
		Location lastLocation;
		Vector vel;
		int taskId;
		int groundCount = 0;
		
		public ItemProjectile(Player caster, float power) {
			this.caster = caster;
			this.power = power;
			Location location = caster.getEyeLocation().add(0, yOffset, 0);
			location.setPitch(0f);
			if (vertSpeedUsed) {
				this.vel = caster.getLocation().getDirection().setY(0).multiply(speed).setY(vertSpeed);
			} else {
				this.vel = caster.getLocation().getDirection().multiply(speed);
			}
			this.entity = caster.getWorld().dropItem(location, item.clone());
			MagicSpells.getVolatileCodeHandler().setGravity(this.entity, projectileHasGravity);
			playSpellEffects(EffectPosition.PROJECTILE, this.entity);
			playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, caster.getLocation(), this.entity.getLocation(), caster, this.entity);
			this.entity.teleport(location);
			this.entity.setPickupDelay(1000000);
			this.entity.setVelocity(this.vel);
			
			this.taskId = MagicSpells.scheduleRepeatingTask(this, 3, 3);
		}
		
		@Override
		public void run() {
			for (Entity e : this.entity.getNearbyEntities(hitRadius, hitRadius + 0.5, hitRadius)) {
				if (e instanceof LivingEntity && validTargetList.canTarget(this.caster, e)) {
					SpellTargetEvent event = new SpellTargetEvent(ItemProjectileSpell.this, this.caster, (LivingEntity)e, this.power);
					EventUtil.call(event);
					if (!event.isCancelled()) {
						if (spellOnHitEntity != null) spellOnHitEntity.castAtEntity(this.caster, (LivingEntity)e, event.getPower());
						stop();
						return;
					}
				}
			}
			if (this.entity.isOnGround()) {
				this.groundCount++;
			} else {
				this.groundCount = 0;
			}
			if (this.groundCount >= 2) {
				if (spellOnHitGround != null) spellOnHitGround.castAtLocation(this.caster, this.entity.getLocation(), this.power);
				stop();
			}
		}
		
		void stop() {
			this.entity.remove();
			MagicSpells.cancelTask(this.taskId);
		}
		
	}

}
