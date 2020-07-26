package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class ThrowBlockSpell extends InstantSpell implements TargetedLocationSpell {

	MagicMaterial material;
	int tntFuse;
	float velocity;
	boolean applySpellPowerToVelocity;
	float verticalAdjustment;
	float yOffset;
	int rotationOffset;
	float fallDamage;
	int fallDamageMax;
	boolean dropItem;
	boolean removeBlocks;
	boolean preventBlocks;
	boolean callTargetEvent;
	boolean checkPlugins;
	boolean ensureSpellCast;
	boolean stickyBlocks;
	boolean projectileHasGravity;
	String spellOnLand;
	TargetedLocationSpell spell;
	
	Map<Entity, FallingBlockInfo> fallingBlocks;
	int cleanTask = -1;
	
	public ThrowBlockSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		String blockTypeInfo = getConfigString("block-type", "anvil");
		if (blockTypeInfo.toLowerCase().startsWith("primedtnt:")) {
			String[] split = blockTypeInfo.split(":");
			this.material = null;
			this.tntFuse = Integer.parseInt(split[1]);
		} else {
			this.material = MagicSpells.getItemNameResolver().resolveBlock(blockTypeInfo);
			this.tntFuse = 0;
		}
		this.velocity = getConfigFloat("velocity", 1);
		this.applySpellPowerToVelocity = getConfigBoolean("apply-spell-power-to-velocity", false);
		this.verticalAdjustment = getConfigFloat("vertical-adjustment", 0.5F);
		this.yOffset = getConfigFloat("y-offset", 0F);
		this.rotationOffset = getConfigInt("rotation-offset", 0);
		this.fallDamage = getConfigFloat("fall-damage", 2.0F);
		this.fallDamageMax = getConfigInt("fall-damage-max", 20);
		this.dropItem = getConfigBoolean("drop-item", false);
		this.removeBlocks = getConfigBoolean("remove-blocks", false);
		this.preventBlocks = getConfigBoolean("prevent-blocks", false);
		this.callTargetEvent = getConfigBoolean("call-target-event", true);
		this.checkPlugins = getConfigBoolean("check-plugins", false);
		this.ensureSpellCast = getConfigBoolean("ensure-spell-cast", true);
		this.stickyBlocks = getConfigBoolean("sticky-blocks", false);
		this.spellOnLand = getConfigString("spell-on-land", null);
		this.projectileHasGravity = getConfigBoolean("gravity", true);
	}	
	
	@Override
	public void initialize() {
		super.initialize();
		if (this.material == null && tntFuse == 0) {
			MagicSpells.error("Invalid block-type for " + this.internalName + " spell");
		}
		if (this.spellOnLand != null && !this.spellOnLand.isEmpty()) {
			Spell s = MagicSpells.getSpellByInternalName(this.spellOnLand);
			if (s instanceof TargetedLocationSpell) {
				this.spell = (TargetedLocationSpell)s;
			} else {
				MagicSpells.error("Invalid spell-on-land for " + this.internalName + " spell");
			}
		}
		if (this.fallDamage > 0 || this.removeBlocks || this.preventBlocks || this.spell != null || this.ensureSpellCast || this.stickyBlocks) {
			this.fallingBlocks = new HashMap<>();
			if (this.material != null) {
				registerEvents(new ThrowBlockListener(this));
			} else if (this.tntFuse > 0) {
				registerEvents(new TntListener());
			}
		}
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Vector v = getVector(player.getLocation(), power);
			Location l = player.getEyeLocation().add(v);
			l.add(0, this.yOffset, 0);
			spawnFallingBlock(player, power, l, v);
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private Vector getVector(Location loc, float power) {
		Vector v = loc.getDirection();
		if (this.verticalAdjustment != 0) v.setY(v.getY() + this.verticalAdjustment);
		if (this.rotationOffset != 0) Util.rotateVector(v, this.rotationOffset);
		v.normalize().multiply(this.velocity);
		if (this.applySpellPowerToVelocity) v.multiply(power);
		return v;
	}
	
	private void spawnFallingBlock(Player player, float power, Location location, Vector velocity) {
		Entity entity = null;
		FallingBlockInfo info = new FallingBlockInfo(player, power);
		if (this.material != null) {
			FallingBlock block = this.material.spawnFallingBlock(location);
			MagicSpells.getVolatileCodeHandler().setGravity(block, this.projectileHasGravity);
			playSpellEffects(EffectPosition.PROJECTILE, block);
			playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, player.getLocation(), block.getLocation(), player, block);
			block.setVelocity(velocity);
			block.setDropItem(this.dropItem);
			if (this.fallDamage > 0) MagicSpells.getVolatileCodeHandler().setFallingBlockHurtEntities(block, this.fallDamage, this.fallDamageMax);
			if (this.ensureSpellCast || this.stickyBlocks) new ThrowBlockMonitor(block, info);
			entity = block;
		} else if (this.tntFuse > 0) {
			TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
			MagicSpells.getVolatileCodeHandler().setGravity(tnt, this.projectileHasGravity);
			playSpellEffects(EffectPosition.PROJECTILE, tnt);
			playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, player.getLocation(), tnt.getLocation(), player, tnt);
			tnt.setFuseTicks(this.tntFuse);
			tnt.setVelocity(velocity);
			entity = tnt;
		}
		if (this.fallingBlocks != null) {
			this.fallingBlocks.put(entity, info);
			if (this.cleanTask < 0) startTask();
		}
	}
	
	void startTask() {
		cleanTask = Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
			@Override
			public void run() {
				Iterator<Entity> iter = fallingBlocks.keySet().iterator();
				while (iter.hasNext()) {
					Entity entity = iter.next();
					if (entity instanceof FallingBlock) {
						FallingBlock block = (FallingBlock)entity;
						if (!block.isValid()) {
							iter.remove();
							if (removeBlocks) {
								Block b = block.getLocation().getBlock();
								if (material.equals(b) || (material.getMaterial() == Material.ANVIL && b.getType() == Material.ANVIL)) {
									b.setType(Material.AIR);
									playSpellEffects(EffectPosition.BLOCK_DESTRUCTION, block.getLocation());
								}
							}
						}
					} else if (entity instanceof TNTPrimed) {
						TNTPrimed tnt = (TNTPrimed)entity;
						if (!tnt.isValid() || tnt.isDead()) iter.remove();
					}
				}
				if (fallingBlocks.isEmpty()) {
					cleanTask = -1;
				} else {
					startTask();
				}
			}
		}, 500);
	}

	class ThrowBlockMonitor implements Runnable {
		
		FallingBlock block;
		FallingBlockInfo info;
		int task;
		int counter = 0;
		
		public ThrowBlockMonitor(FallingBlock fallingBlock, FallingBlockInfo fallingBlockInfo) {
			this.block = fallingBlock;
			this.info = fallingBlockInfo;
			this.task = MagicSpells.scheduleRepeatingTask(this, TimeUtil.TICKS_PER_SECOND, 1);
		}
		
		@Override
		public void run() {
			if (stickyBlocks && !this.block.isDead()) {
				if (this.block.getVelocity().lengthSquared() < .01) {
					if (!preventBlocks) {
						Block b = this.block.getLocation().getBlock();
						if (b.getType() == Material.AIR) BlockUtils.setBlockFromFallingBlock(b, this.block, true);
					}
					if (!this.info.spellActivated && spell != null) {
						if (this.info.player != null) {
							spell.castAtLocation(this.info.player, this.block.getLocation(), this.info.power);
						} else {
							spell.castAtLocation(this.block.getLocation(), this.info.power);
						}
						info.spellActivated = true;
					}
					this.block.remove();
				}
			}
			if (ensureSpellCast && this.block.isDead()) {
				if (!this.info.spellActivated && spell != null) {
					if (this.info.player != null) {
						spell.castAtLocation(this.info.player, this.block.getLocation(), this.info.power);
					} else {
						spell.castAtLocation(this.block.getLocation(), this.info.power);
					}
				}
				this.info.spellActivated = true;
				MagicSpells.cancelTask(this.task);
			}
			if (this.counter++ > 1500) MagicSpells.cancelTask(this.task);
		}
		
	}

	class ThrowBlockListener implements Listener {
		
		ThrowBlockSpell thisSpell;
		
		public ThrowBlockListener(ThrowBlockSpell throwBlockSpell) {
			this.thisSpell = throwBlockSpell;
		}
		
		@EventHandler(ignoreCancelled=true)
		public void onDamage(EntityDamageByEntityEvent event) {
			FallingBlockInfo info;
			if (removeBlocks || preventBlocks) {
				info = fallingBlocks.get(event.getDamager());
			} else {
				info = fallingBlocks.remove(event.getDamager());
			}
			if (info != null && event.getEntity() instanceof LivingEntity) {
				float power = info.power;
				if (callTargetEvent && info.player != null) {
					SpellTargetEvent evt = new SpellTargetEvent(this.thisSpell, info.player, (LivingEntity)event.getEntity(), power);
					EventUtil.call(evt);
					if (evt.isCancelled()) {
						event.setCancelled(true);
						return;
					}
					power = evt.getPower();
				}
				double damage = event.getDamage() * power;
				if (checkPlugins && info.player != null) {
					MagicSpellsEntityDamageByEntityEvent evt = new MagicSpellsEntityDamageByEntityEvent(info.player, event.getEntity(), DamageCause.ENTITY_ATTACK, damage);
					EventUtil.call(evt);
					if (evt.isCancelled()) {
						event.setCancelled(true);
						return;
					}
				}
				event.setDamage(damage);
				if (spell != null && !info.spellActivated) {
					if (info.player != null) {
						spell.castAtLocation(info.player, event.getEntity().getLocation(), power);
					} else {
						spell.castAtLocation(event.getEntity().getLocation(), power);
					}
					info.spellActivated = true;
				}
			}
		}
		
		@EventHandler(ignoreCancelled=true)
		public void onBlockLand(EntityChangeBlockEvent event) {
			if (!preventBlocks && spell == null) return;
			FallingBlockInfo info = fallingBlocks.get(event.getEntity());
			if (info == null) return;
			if (preventBlocks) {
				event.getEntity().remove();
				event.setCancelled(true);
			}
			if (spell != null && !info.spellActivated) {
				if (info.player != null) {
					spell.castAtLocation(info.player, event.getBlock().getLocation().add(.5, .5, .5), info.power);
				} else {
					spell.castAtLocation(event.getBlock().getLocation().add(.5, .5, .5), info.power);
				}
				info.spellActivated = true;
			}
		}
	
	}
	
	class TntListener implements Listener {
		
		@EventHandler
		void onExplode(EntityExplodeEvent event) {
			FallingBlockInfo info = fallingBlocks.get(event.getEntity());
			if (info == null) return;
			if (preventBlocks) {
				event.blockList().clear();
				event.setYield(0f);
				event.setCancelled(true);
				event.getEntity().remove();
			}
			if (spell != null && !info.spellActivated) {
				if (info.player != null) {
					spell.castAtLocation(info.player, event.getEntity().getLocation(), info.power);
				} else {
					spell.castAtLocation(event.getEntity().getLocation(), info.power);
				}
				info.spellActivated = true;
			}
		}
		
	}
		
	@Override
	public void turnOff() {
		if (fallingBlocks != null) fallingBlocks.clear();
	}
	
	class FallingBlockInfo {
		
		Player player;
		float power;
		boolean spellActivated;
		
		public FallingBlockInfo(Player caster, float castPower) {
			this.player = caster;
			this.power = castPower;
			this.spellActivated = false;
		}
		
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		Vector v = getVector(target, power);
		spawnFallingBlock(caster, power, target.clone().add(0, this.yOffset, 0), v);
		playSpellEffects(EffectPosition.CASTER, target);
		return true;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		Vector v = getVector(target, power);
		spawnFallingBlock(null, power, target.clone().add(0, this.yOffset, 0), v);
		playSpellEffects(EffectPosition.CASTER, target);
		return true;
	}

}
