package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class ProjectileSpell extends InstantSpell {

	private Class<? extends Projectile> projectileClass;
	private ItemStack projectileItem;
	private double velocity;
	private double horizSpread;
	private double vertSpread;
	private boolean applySpellPowerToVelocity;
	private boolean requireHitEntity;
	boolean cancelDamage;
	boolean removeProjectile;
	private int maxDistanceSquared;
	int effectInterval;
	private List<String> spellNames;
	private List<Subspell> spells;
	private int aoeRadius;
	private boolean targetPlayers;
	private boolean allowTargetChange;
	private String strHitCaster;
	private String strHitTarget;
	private boolean projectileHasGravity;
	
	HashMap<Projectile, ProjectileInfo> projectiles;
	HashMap<Item, ProjectileInfo> itemProjectiles;
	
	private Random random = new Random();
	
	private static final String METADATA_KEY = "MagicSpellsSource";
	
	public ProjectileSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		String projectileType = getConfigString("projectile", "arrow");
		if (projectileType.equalsIgnoreCase("arrow")) {
			this.projectileClass = Arrow.class;
		} else if (projectileType.equalsIgnoreCase("snowball")) {
			this.projectileClass = Snowball.class;
		} else if (projectileType.equalsIgnoreCase("egg")) {
			this.projectileClass = Egg.class;
		} else if (projectileType.equalsIgnoreCase("enderpearl")) {
			this.projectileClass = EnderPearl.class;
		} else if (projectileType.equalsIgnoreCase("potion")) {
			this.projectileClass = ThrownPotion.class;
		} else {
			ItemStack item = Util.getItemStackFromString(projectileType);
			if (!InventoryUtil.isNothing(item)) {
				item.setAmount(0);
				this.projectileItem = item;
			}
		}
		if (this.projectileClass == null && this.projectileItem == null) {
			MagicSpells.error("Invalid projectile type on spell '" + this.internalName + '\'');
		}
		this.velocity = getConfigFloat("velocity", 0);
		this.horizSpread = getConfigFloat("horizontal-spread", 0);
		this.vertSpread = getConfigFloat("vertical-spread", 0);
		this.applySpellPowerToVelocity = getConfigBoolean("apply-spell-power-to-velocity", false);
		this.requireHitEntity = getConfigBoolean("require-hit-entity", false);
		this.cancelDamage = getConfigBoolean("cancel-damage", true);
		this.removeProjectile = getConfigBoolean("remove-projectile", true);
		this.maxDistanceSquared = getConfigInt("max-distance", 0);
		this.maxDistanceSquared = this.maxDistanceSquared * this.maxDistanceSquared;
		this.effectInterval = getConfigInt("effect-interval", 0);
		this.spellNames = getConfigStringList("spells", null);
		this.aoeRadius = getConfigInt("aoe-radius", 0);
		this.targetPlayers = getConfigBoolean("target-players", false);
		this.allowTargetChange = getConfigBoolean("allow-target-change", true);
		this.projectileHasGravity = getConfigBoolean("gravity", true);
		this.strHitCaster = getConfigString("str-hit-caster", "");
		this.strHitTarget = getConfigString("str-hit-target", "");
		
		if (this.projectileClass != null) {
			this.projectiles = new HashMap<>();
		} else if (this.projectileItem != null) {
			this.itemProjectiles = new HashMap<>();
		}
	}
	
	@Override
	public void initialize() {
		super.initialize();
		this.spells = new ArrayList<>();
		if (this.spellNames != null) {
			for (String spellName : this.spellNames) {
				Subspell spell = new Subspell(spellName);
				if (spell.process()) {
					this.spells.add(spell);
				} else {
					MagicSpells.error("Projectile spell '" + this.internalName + "' attempted to add invalid spell '" + spellName + "'.");
				}
			}
		}
		if (this.spells.isEmpty()) {
			MagicSpells.error("Projectile spell '" + this.internalName + "' has no spells!");
		}
		
		if (this.projectileClass != null) {
			if (this.projectileClass == EnderPearl.class) {
				registerEvents(new EnderTpListener());
			} else if (this.projectileClass == Egg.class) {
				registerEvents(new EggListener());
			} else if (this.projectileClass == ThrownPotion.class) {
				registerEvents(new PotionListener());
			}
			registerEvents(new ProjectileListener());
		} else if (this.projectileItem != null) {
			registerEvents(new PickupListener());
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (this.projectileClass != null) {
				Projectile projectile = player.launchProjectile(this.projectileClass);
				playSpellEffects(EffectPosition.PROJECTILE, projectile);
				playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, player.getLocation(), projectile.getLocation(), player, projectile);
				projectile.setBounce(false);
				MagicSpells.getVolatileCodeHandler().setGravity(projectile, this.projectileHasGravity);
				if (this.velocity > 0) {
					projectile.setVelocity(player.getLocation().getDirection().multiply(this.velocity));
				}
				if (this.horizSpread > 0 || this.vertSpread > 0) {
					Vector v = projectile.getVelocity();
					v.add(new Vector((this.random.nextDouble() - .5) * this.horizSpread, (this.random.nextDouble() - .5) * this.vertSpread, (this.random.nextDouble() - .5) * this.horizSpread));
					projectile.setVelocity(v);
				}
				if (this.applySpellPowerToVelocity) {
					projectile.setVelocity(projectile.getVelocity().multiply(power));
				}
				projectile.setMetadata(METADATA_KEY, new FixedMetadataValue(MagicSpells.plugin, "ProjectileSpell_" + this.internalName));
				this.projectiles.put(projectile, new ProjectileInfo(player, power, (this.effectInterval > 0 ? new RegularProjectileMonitor(projectile) : null)));
				playSpellEffects(EffectPosition.CASTER, projectile);
			} else if (this.projectileItem != null) {
				Item item = player.getWorld().dropItem(player.getEyeLocation(), this.projectileItem.clone());
				MagicSpells.getVolatileCodeHandler().setGravity(item, this.projectileHasGravity);
				Vector v = player.getLocation().getDirection().multiply(this.velocity > 0 ? this.velocity : 1);
				if (this.horizSpread > 0 || this.vertSpread > 0) {
					v.add(new Vector((this.random.nextDouble() - .5) * this.horizSpread, (this.random.nextDouble() - .5) * this.vertSpread, (this.random.nextDouble() - .5) * this.horizSpread));
				}
				if (this.applySpellPowerToVelocity) {
					v.multiply(power);
				}
				item.setVelocity(v);
				item.setPickupDelay(10);
				this.itemProjectiles.put(item, new ProjectileInfo(player, power, new ItemProjectileMonitor(item)));
				playSpellEffects(EffectPosition.CASTER, item);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public boolean projectileHitEntity(Entity projectile, LivingEntity target, ProjectileInfo info) {
		// TODO flatten
		if (!info.done && (this.maxDistanceSquared == 0 || projectile.getLocation().distanceSquared(info.start) <= this.maxDistanceSquared)) {
			if (this.aoeRadius == 0) {
				float power = info.power;
								
				// Check player
				if (!this.targetPlayers && target instanceof Player) return false;
				
				// Call target event
				SpellTargetEvent evt = new SpellTargetEvent(this, info.player, target, power);
				EventUtil.call(evt);
				if (evt.isCancelled()) return false;
				if (this.allowTargetChange) {
					target = evt.getTarget();
					power = evt.getPower();
				}
				
				// Run spells
				for (Subspell spell : this.spells) {
					if (spell.isTargetedEntitySpell()) {
						spell.castAtEntity(info.player, target, power);
						playSpellEffects(EffectPosition.TARGET, target);
					} else if (spell.isTargetedLocationSpell()) {
						spell.castAtLocation(info.player, target.getLocation(), power);
						playSpellEffects(EffectPosition.TARGET, target.getLocation());
					}
				}
				
				// Send messages
				String entityName;
				if (target instanceof Player) {
					entityName = ((Player)target).getDisplayName();
				} else {
					EntityType entityType = target.getType();
					entityName = MagicSpells.getEntityNames().get(entityType);
					if (entityName == null) {
						entityName = entityType.name().toLowerCase();
					}
				}
				sendMessage(formatMessage(this.strHitCaster, "%t", entityName), info.player, MagicSpells.NULL_ARGS);
				if (target instanceof Player) {
					sendMessage(formatMessage(this.strHitTarget, "%a", info.player.getDisplayName()), (Player)target, MagicSpells.NULL_ARGS);
				}
			} else {
				aoe(projectile, info);
			}
			
			info.done = true;
			
		}
		return true;
	}
	
	boolean projectileHitLocation(Entity projectile, ProjectileInfo info) {
		if (!this.requireHitEntity && !info.done && (this.maxDistanceSquared == 0 || projectile.getLocation().distanceSquared(info.start) <= this.maxDistanceSquared)) {
			if (this.aoeRadius == 0) {
				for (Subspell spell : this.spells) {
					if (spell.isTargetedLocationSpell()) {
						Location loc = projectile.getLocation();
						Util.setLocationFacingFromVector(loc, projectile.getVelocity());
						spell.castAtLocation(info.player, loc, info.power);
						playSpellEffects(EffectPosition.TARGET, loc);
					}
				}
				sendMessage(this.strHitCaster, info.player, MagicSpells.NULL_ARGS);
			} else {
				aoe(projectile, info);
			}
			info.done = true;
		}
		return true;
	}
		
	private void aoe(Entity projectile, ProjectileInfo info) {
		playSpellEffects(EffectPosition.SPECIAL, projectile.getLocation());
		List<Entity> entities = projectile.getNearbyEntities(this.aoeRadius, this.aoeRadius, this.aoeRadius);
		for (Entity entity : entities) {
			if (!(entity instanceof LivingEntity)) continue;
			
			if ((this.targetPlayers || !(entity instanceof Player)) && !entity.equals(info.player)) {
				LivingEntity target = (LivingEntity)entity;
				float power = info.power;
				
				// Call target event
				SpellTargetEvent evt = new SpellTargetEvent(this, info.player, target, power);
				EventUtil.call(evt);
				if (evt.isCancelled()) continue;
				if (this.allowTargetChange) {
					target = evt.getTarget();
				}
				power = evt.getPower();
				
				// Run spells
				for (Subspell spell : this.spells) {
					if (spell.isTargetedEntitySpell()) {
						spell.castAtEntity(info.player, target, power);
						playSpellEffects(EffectPosition.TARGET, target);
					} else if (spell.isTargetedLocationSpell()) {
						spell.castAtLocation(info.player, target.getLocation(), power);
						playSpellEffects(EffectPosition.TARGET, target.getLocation());
					}
				}
				
				// Send message if player
				if (target instanceof Player) {
					sendMessage(formatMessage(this.strHitTarget, "%a", info.player.getDisplayName()), (Player)target, MagicSpells.NULL_ARGS);
				}
			}
		}
		sendMessage(strHitCaster, info.player, MagicSpells.NULL_ARGS);
	}
	
	public class ProjectileListener implements Listener {
		
		@EventHandler(priority=EventPriority.HIGHEST)
		public void onEntityDamage(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Projectile)) return;
			
			Projectile projectile = (Projectile)event.getDamager();
			ProjectileInfo info = projectiles.get(projectile);
			if (info == null || event.isCancelled()) return;
			
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			projectileHitEntity(projectile, (LivingEntity)event.getEntity(), info);		
			
			if (cancelDamage) event.setCancelled(true);
			
			if (info.monitor != null) info.monitor.stop();
		}
		
		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			final Projectile projectile = event.getEntity();
			ProjectileInfo info = projectiles.get(projectile);
			if (info != null) {
				projectileHitLocation(projectile, info);
				
				// Remove it from world
				if (removeProjectile) projectile.remove();
				
				// Remove it at end of tick
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, () -> projectiles.remove(projectile), 0);
				
				if (info.monitor != null) info.monitor.stop();
			}
		}
		
	}
	
	public class EnderTpListener implements Listener {
		
		@EventHandler(ignoreCancelled=true)
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			if (event.getCause() == TeleportCause.ENDER_PEARL) {
				for (Projectile projectile : projectiles.keySet()) {
					if (locationsEqual(projectile.getLocation(), event.getTo())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		
	}
	
	public class EggListener implements Listener {
		
		@EventHandler(ignoreCancelled=true)
		public void onCreatureSpawn(CreatureSpawnEvent event) {
			if (event.getSpawnReason() == SpawnReason.EGG) {
				for (Projectile projectile : projectiles.keySet()) {
					if (locationsEqual(projectile.getLocation(), event.getLocation())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		
	}
	
	public class PotionListener implements Listener {
		
		@EventHandler(ignoreCancelled=true)
		public void onPotionSplash(PotionSplashEvent event) {
			if (projectiles.containsKey(event.getPotion())) event.setCancelled(true);
		}
		
	}
	
	public class PickupListener implements Listener {
		
		@EventHandler(ignoreCancelled=true)
		public void onPickupItem(PlayerPickupItemEvent event) {
			Item item = event.getItem();
			ProjectileInfo info = itemProjectiles.get(item);
			if (info == null) return;
			event.setCancelled(true);
			projectileHitEntity(item, event.getPlayer(), info);
			item.remove();
			itemProjectiles.remove(item);
			info.monitor.stop();
		}
		
	}
	
	boolean locationsEqual(Location loc1, Location loc2) {
		return 
				Math.abs(loc1.getX() - loc2.getX()) < 0.1
				&& Math.abs(loc1.getY() - loc2.getY()) < 0.1
				&& Math.abs(loc1.getZ() - loc2.getZ()) < 0.1;
	}
	
	private class ProjectileInfo {
		
		Player player;
		Location start;
		float power;
		boolean done;
		ProjectileMonitor monitor;
		
		public ProjectileInfo(Player player, float power) {
			this.player = player;
			this.start = player.getLocation();
			this.power = power;
			this.done = false;
			this.monitor = null;
		}
		
		public ProjectileInfo(Player player, float power, ProjectileMonitor monitor) {
			this(player, power);
			this.monitor = monitor;
		}
		
	}
	
	private interface ProjectileMonitor {
		
		void stop();
		
	}
	
	private class ItemProjectileMonitor implements Runnable, ProjectileMonitor {

		Item item;
		int taskId;
		int count;
		
		public ItemProjectileMonitor(Item item) {
			this.item = item;
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 1, 1);
			this.count = 0;
		}
		
		@Override
		public void run() {
			Vector v = item.getVelocity();
			if (Math.abs(v.getY()) < .01 || (Math.abs(v.getX()) < .01 && Math.abs(v.getZ()) < .01)) {
				ProjectileInfo info = itemProjectiles.get(item);
				if (info != null) {
					projectileHitLocation(item, info);
					stop();
				}
			}
			if (effectInterval > 0 && count % effectInterval == 0) {
				playSpellEffects(EffectPosition.SPECIAL, item.getLocation());
			}
			if (++count > 300) {
				stop();
			}
		}
		
		@Override
		public void stop() {
			item.remove();
			itemProjectiles.remove(item);
			Bukkit.getScheduler().cancelTask(taskId);
		}
		
	}
	
	private class RegularProjectileMonitor implements Runnable, ProjectileMonitor {
		
		Projectile projectile;
		Location prevLoc;
		int taskId;
		int count = 0;
		
		public RegularProjectileMonitor(Projectile projectile) {
			this.projectile = projectile;
			this.prevLoc = projectile.getLocation();
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, effectInterval, effectInterval);
		}
		
		@Override
		public void run() {
			playSpellEffects(EffectPosition.SPECIAL, prevLoc);
			prevLoc = projectile.getLocation();
			
			if (!projectile.isValid() || projectile.isOnGround()) stop();
			
			if (count++ > 100) stop();
		}
		
		@Override
		public void stop() {
			Bukkit.getScheduler().cancelTask(taskId);
		}
		
	}

}
