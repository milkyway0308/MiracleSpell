package com.nisovin.magicspells.caster;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public abstract class LivingEntityCaster {

/*	private LivingEntity entity;
	
	private boolean allowFlight = false;

	private boolean hasPlayedBefore;

	private Location bedSpawnLocation;

	private Location compassTarget;

	private String displayName;

	private float exhaustion;

	private float exp;

	private int foodLevel;

	private float flySpeed;

	private double healthScale;

	private int level;

	private String playerListName;

	private long playerTime;

	private long playerTimeOffset;

	private WeatherType playerWeather;

	private float saturation;

	private Scoreboard scoreboard;

	private int totalExperience;

	private float walkSpeed;
	
	public LivingEntityCaster(LivingEntity e) {
		this.entity = e;
	}
	
	public void awardAchievement(Achievement arg0) {
		//no op
	}

	public boolean canSee(Player arg0) {
		//TODO fix this up
		return false;
	}

	
	public void chat(String arg0) {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0)
			throws IllegalArgumentException {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		//no op
	}

	
	public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
		//no op
	}

	
	public InetSocketAddress getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean getAllowFlight() {
		
		//TODO make this configurable
		return this.allowFlight;
	}

	
	public Location getBedSpawnLocation() {
		return this.bedSpawnLocation;
	}

	
	public Location getCompassTarget() {
		return this.compassTarget;
	}

	
	public String getDisplayName() {
		return this.displayName;
	}

	
	public float getExhaustion() {
		return this.exhaustion;
	}

	
	public float getExp() {
		return this.exp;
	}

	
	public float getFlySpeed() {
		return this.flySpeed;
	}

	
	public int getFoodLevel() {
		return this.foodLevel;
	}

	
	public double getHealthScale() {
		return this.healthScale;
	}

	
	public int getLevel() {
		return this.level;
	}

	
	public String getPlayerListName() {
		return this.playerListName;
	}

	
	public long getPlayerTime() {
		return this.playerTime;
	}

	
	public long getPlayerTimeOffset() {
		return this.playerTimeOffset;
	}

	
	public WeatherType getPlayerWeather() {
		return this.playerWeather;
	}

	
	public float getSaturation() {
		return this.saturation;
	}

	
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	
	public int getStatistic(Statistic arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getTotalExperience() {
		return this.totalExperience;
	}

	
	public float getWalkSpeed() {
		return this.walkSpeed;
	}

	
	public void giveExp(int arg0) {
		// TODO Auto-generated method stub
	}

	
	public void giveExpLevels(int arg0) {
		// TODO Auto-generated method stub
	}

	
	public boolean hasAchievement(Achievement arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void hidePlayer(Player arg0) {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0, Material arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0, EntityType arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	
	public boolean isFlying() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isHealthScaled() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isOnGround() {
		return entity.isOnGround();
	}

	
	public boolean isPlayerTimeRelative() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isSleepingIgnored() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isSneaking() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isSprinting() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void kickPlayer(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void loadData() {
		// TODO Auto-generated method stub
		
	}

	
	public boolean performCommand(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void playEffect(Location arg0, Effect arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void playNote(Location arg0, byte arg1, byte arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void playNote(Location arg0, Instrument arg1, Note arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		
	}

	
	public void playSound(Location arg0, String arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		
	}

	
	public void removeAchievement(Achievement arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void resetPlayerTime() {
		// TODO Auto-generated method stub
		
	}

	
	public void resetPlayerWeather() {
		// TODO Auto-generated method stub
		
	}

	
	public void saveData() {
		// TODO Auto-generated method stub
		
	}

	
	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void sendBlockChange(Location arg0, int arg1, byte arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3,
			byte[] arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void sendMap(MapView arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void sendRawMessage(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void sendSignChange(Location arg0, String[] arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void setAllowFlight(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setBedSpawnLocation(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public void setCompassTarget(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setDisplayName(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setExhaustion(float arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setExp(float arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void setFlying(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setFoodLevel(int arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setHealthScale(double arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void setHealthScaled(boolean arg0) {
		// TODO Auto-generated method stub
	}

	
	public void setLevel(int arg0) {
		// TODO Auto-generated method stub
	}

	
	public void setPlayerListName(String arg0) {
		// TODO Auto-generated method stub
	}

	
	public void setPlayerTime(long arg0, boolean arg1) {
		//no op
	}

	
	public void setPlayerWeather(WeatherType arg0) {
		//no op
	}

	
	public void setResourcePack(String arg0) {
		//no op
	}

	
	public void setSaturation(float arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException,
			IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	
	public void setSleepingIgnored(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setSneaking(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setSprinting(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setStatistic(Statistic arg0, int arg1)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void setStatistic(Statistic arg0, Material arg1, int arg2)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void setTexturePack(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setTotalExperience(int arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	
	public void showPlayer(Player arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void updateInventory() {
		// TODO Auto-generated method stub
		
	}

	
	public void closeInventory() {
		// TODO Auto-generated method stub
		
	}

	
	public Inventory getEnderChest() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getExpToLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public GameMode getGameMode() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public PlayerInventory getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public ItemStack getItemInHand() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public ItemStack getItemOnCursor() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public InventoryView getOpenInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getSleepTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isSleeping() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public InventoryView openInventory(Inventory arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void openInventory(InventoryView arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setGameMode(GameMode arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setItemInHand(ItemStack arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setItemOnCursor(ItemStack arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public boolean setWindowProperty(Property arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public int _INVALID_getLastDamage() {
		return entity._INVALID_getLastDamage();
	}

	
	public void _INVALID_setLastDamage(int arg0) {
		entity._INVALID_setLastDamage(arg0);
	}

	
	public boolean addPotionEffect(PotionEffect arg0) {
		return entity.addPotionEffect(arg0);
	}

	
	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		return entity.addPotionEffect(arg0, arg1);
	}

	
	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		return entity.addPotionEffects(arg0);
	}

	
	public Collection<PotionEffect> getActivePotionEffects() {
		return entity.getActivePotionEffects();
	}

	
	public boolean getCanPickupItems() {
		return entity.getCanPickupItems();
	}

	
	public EntityEquipment getEquipment() {
		return entity.getEquipment();
	}

	
	public double getEyeHeight() {
		return entity.getEyeHeight();
	}

	
	public double getEyeHeight(boolean arg0) {
		return entity.getEyeHeight(arg0);
	}

	
	public Location getEyeLocation() {
		return entity.getEyeLocation();
	}

	
	public Player getKiller() {
		return entity.getKiller();
	}

	
	public double getLastDamage() {
		return entity.getLastDamage();
	}

	
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		return entity.getLastTwoTargetBlocks(arg0, arg1);
	}

	
	public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
		return entity.getLastTwoTargetBlocks(arg0, arg1);
	}

	
	public Entity getLeashHolder() throws IllegalStateException {
		return entity.getLeashHolder();
	}

	
	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		return entity.getLineOfSight(arg0, arg1);
	}

	
	public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
		return entity.getLineOfSight(arg0, arg1);
	}

	
	public int getMaximumAir() {
		return entity.getMaximumAir();
	}

	
	public int getMaximumNoDamageTicks() {
		return entity.getMaximumNoDamageTicks();
	}

	
	public int getNoDamageTicks() {
		return entity.getNoDamageTicks();
	}

	
	public int getRemainingAir() {
		return entity.getRemainingAir();
	}

	
	public boolean getRemoveWhenFarAway() {
		return entity.getRemoveWhenFarAway();
	}

	
	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		return entity.getTargetBlock(arg0, arg1);
	}

	
	public Block getTargetBlock(Set<Material> arg0, int arg1) {
		return entity.getTargetBlock(arg0, arg1);
	}

	
	public boolean hasLineOfSight(Entity arg0) {
		return entity.hasLineOfSight(arg0);
	}

	
	public boolean hasPotionEffect(PotionEffectType arg0) {
		return entity.hasPotionEffect(arg0);
	}

	
	public boolean isLeashed() {
		return entity.isLeashed();
	}

	
	public void removePotionEffect(PotionEffectType arg0) {
		entity.removePotionEffect(arg0);
	}

	
	public void setCanPickupItems(boolean arg0) {
		entity.setCanPickupItems(arg0);
	}

	
	public void setLastDamage(double arg0) {
		entity.setLastDamage(arg0);
	}

	
	public boolean setLeashHolder(Entity arg0) {
		return entity.setLeashHolder(arg0);
	}

	
	public void setMaximumAir(int arg0) {
		entity.setMaximumAir(arg0);
	}

	
	public void setMaximumNoDamageTicks(int arg0) {
		entity.setMaximumNoDamageTicks(arg0);
	}

	
	public void setNoDamageTicks(int arg0) {
		entity.setNoDamageTicks(arg0);
	}

	
	public void setRemainingAir(int arg0) {
		entity.setRemainingAir(arg0);
	}

	
	public void setRemoveWhenFarAway(boolean arg0) {
		entity.setRemoveWhenFarAway(arg0);
	}

	
	public Arrow shootArrow() {
		return entity.shootArrow();
	}

	
	public Egg throwEgg() {
		return entity.throwEgg();
	}

	
	public Snowball throwSnowball() {
		return entity.throwSnowball();
	}

	
	public boolean eject() {
		return entity.eject();
	}

	
	public String getCustomName() {
		return entity.getCustomName();
	}

	
	public int getEntityId() {
		return entity.getEntityId();
	}

	
	public float getFallDistance() {
		return entity.getFallDistance();
	}

	
	public int getFireTicks() {
		return entity.getFireTicks();
	}

	
	public EntityDamageEvent getLastDamageCause() {
		return entity.getLastDamageCause();
	}

	
	public Location getLocation() {
		return entity.getLocation();
	}

	
	public Location getLocation(Location arg0) {
		return entity.getLocation(arg0);
	}

	
	public int getMaxFireTicks() {
		return entity.getMaxFireTicks();
	}

	
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		return entity.getNearbyEntities(arg0, arg1, arg2);
	}

	
	public Entity getPassenger() {
		return entity.getPassenger();
	}

	
	public Server getServer() {
		return entity.getServer();
	}

	
	public int getTicksLived() {
		return entity.getTicksLived();
	}

	
	public EntityType getType() {
		return entity.getType();
	}

	
	public UUID getUniqueId() {
		return entity.getUniqueId();
	}

	
	public Entity getVehicle() {
		return entity.getVehicle();
	}

	
	public Vector getVelocity() {
		return entity.getVelocity();
	}

	
	public World getWorld() {
		return entity.getWorld();
	}

	
	public boolean isCustomNameVisible() {
		return entity.isCustomNameVisible();
	}

	
	public boolean isDead() {
		return entity.isDead();
	}

	
	public boolean isEmpty() {
		return entity.isEmpty();
	}

	
	public boolean isInsideVehicle() {
		return entity.isInsideVehicle();
	}

	
	public boolean isValid() {
		return entity.isValid();
	}

	
	public boolean leaveVehicle() {
		return entity.leaveVehicle();
	}

	
	public void playEffect(EntityEffect arg0) {
		entity.playEffect(arg0);
	}

	
	public void remove() {
		entity.remove();
	}

	
	public void setCustomName(String arg0) {
		entity.setCustomName(arg0);
	}

	
	public void setCustomNameVisible(boolean arg0) {
		entity.setCustomNameVisible(arg0);
	}

	
	public void setFallDistance(float arg0) {
		entity.setFallDistance(arg0);
	}

	
	public void setFireTicks(int arg0) {
		entity.setFireTicks(arg0);
	}

	
	public void setLastDamageCause(EntityDamageEvent arg0) {
		entity.setLastDamageCause(arg0);
	}

	
	public boolean setPassenger(Entity arg0) {
		return entity.setPassenger(arg0);
	}

	
	public void setTicksLived(int arg0) {
		entity.setTicksLived(arg0);
	}

	
	public void setVelocity(Vector arg0) {
		entity.setVelocity(arg0);
	}
 
	
	public boolean teleport(Location arg0) {
		return entity.teleport(arg0);
	}
 
	
	public boolean teleport(Entity arg0) {
		return entity.teleport(arg0);
	}

	
	public boolean teleport(Location arg0, TeleportCause arg1) {
		return entity.teleport(arg0, arg1);
	}

	
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		return entity.teleport(arg0, arg1);
	}

	
	public List<MetadataValue> getMetadata(String arg0) {
		return entity.getMetadata(arg0);
	}

	
	public boolean hasMetadata(String arg0) {
		return entity.hasMetadata(arg0);
	}

	
	public void removeMetadata(String arg0, Plugin arg1) {
		entity.removeMetadata(arg0, arg1);
	}

	
	public void setMetadata(String arg0, MetadataValue arg1) {
		entity.setMetadata(arg0, arg1);
	}

	
	public void sendMessage(String arg0) {
		entity.sendMessage(arg0);
	}

	
	public void sendMessage(String[] arg0) {
		entity.sendMessage(arg0);
	}

	
	public PermissionAttachment addAttachment(Plugin arg0) {
		return entity.addAttachment(arg0);
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return entity.addAttachment(arg0, arg1);
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2) {
		return entity.addAttachment(arg0, arg1, arg2);
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3) {
		return entity.addAttachment(arg0, arg1, arg2, arg3);
	}

	
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return entity.getEffectivePermissions();
	}

	
	public boolean hasPermission(String arg0) {
		return entity.hasPermission(arg0);
	}

	
	public boolean hasPermission(Permission arg0) {
		return entity.hasPermission(arg0);
	}

	
	public boolean isPermissionSet(String arg0) {
		return entity.isPermissionSet(arg0);
	}

	
	public boolean isPermissionSet(Permission arg0) {
		return entity.isPermissionSet(arg0);
	}

	
	public void recalculatePermissions() {
		entity.recalculatePermissions();
	}

	
	public void removeAttachment(PermissionAttachment arg0) {
		entity.removeAttachment(arg0);
	}

	
	public boolean isOp() {
		return entity.isOp();
	}

	
	public void setOp(boolean arg0) {
		entity.setOp(arg0);
	}

	
	public void _INVALID_damage(int arg0) {
		//no op
	}

	
	public void _INVALID_damage(int arg0, Entity arg1) {
		//no op
	}

	
	public int _INVALID_getHealth() {
		//no op
		return 0;
	}

	
	public int _INVALID_getMaxHealth() {
		//no op
		return 0;
	}

	
	public void _INVALID_setHealth(int arg0) {
		//no op
	}

	
	public void _INVALID_setMaxHealth(int arg0) {
		//no op
	}

	
	public void damage(double arg0) {
		entity.damage(arg0);
	}

	
	public void damage(double arg0, Entity arg1) {
		entity.damage(arg0, arg1);
	}

	
	public double getHealth() {
		return entity.getHealth();
	}

	
	public double getMaxHealth() {
		return entity.getMaxHealth();
	}

	
	public void resetMaxHealth() {
		entity.resetMaxHealth();
	}

	
	public void setHealth(double arg0) {
		entity.setHealth(arg0);
	}

	
	public void setMaxHealth(double arg0) {
		entity.setMaxHealth(arg0);
	}

	
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		return entity.launchProjectile(arg0);
	}

	
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0,
			Vector arg1) {
		return entity.launchProjectile(arg0, arg1);
	}

	
	public void abandonConversation(Conversation arg0) {
		//no op
	}

	
	public void abandonConversation(Conversation arg0,
			ConversationAbandonedEvent arg1) {
		//no op
	}

	
	public void acceptConversationInput(String arg0) {
		//no op
	}

	
	public boolean beginConversation(Conversation arg0) {
		return false;
	}

	
	public boolean isConversing() {
		return false;
	}

	
	public long getFirstPlayed() {
		return 0;
	}

	
	public long getLastPlayed() {
		return 0;
	}

	
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean hasPlayedBefore() {
		return this.hasPlayedBefore;
	}

	
	public boolean isBanned() {
		return false;
	}

	
	public boolean isOnline() {
		return true;
	}

	
	public boolean isWhitelisted() {
		return true;
	}

	
	public void setBanned(boolean arg0) {
		//no op
	}

	
	public void setWhitelisted(boolean arg0) {
		//no op
	}

	
	public Map<String, Object> serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Set<String> getListeningPluginChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		// TODO Auto-generated method stub
	}
	*/
}
