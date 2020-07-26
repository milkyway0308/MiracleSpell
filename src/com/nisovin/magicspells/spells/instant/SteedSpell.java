package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class SteedSpell extends InstantSpell {

	Random random = new Random();
	boolean gravity;
	Map<String, Integer> mounted = new HashMap<>();
	
	EntityType type;
	Horse.Color color = null;
	Horse.Style style = null;
	Horse.Variant variant = null;
	
	ItemStack armor;
	
	String strAlreadyMounted;
	
	public SteedSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		this.gravity = getConfigBoolean("gravity", true);
		this.type = Util.getEntityType(getConfigString("type", "horse"));
		if (this.type == EntityType.HORSE) {
			String c = getConfigString("color", null);
			String s = getConfigString("style", null);
			String v = getConfigString("variant", null);
			String a = getConfigString("armor", null);
			if (c != null) {
				for (Horse.Color h : Horse.Color.values()) {
					if (!h.name().equalsIgnoreCase(c)) continue;
					this.color = h;
					break;
				}
				if (this.color == null) DebugHandler.debugBadEnumValue(Horse.Color.class, c);
			}
			if (s != null) {
				for (Horse.Style h : Horse.Style.values()) {
					if (!h.name().equalsIgnoreCase(s)) continue;
					this.style = h;
					break;
				}
				if (this.style == null) DebugHandler.debugBadEnumValue(Horse.Style.class, s);
			}
			if (v != null) {
				for (Horse.Variant h : Horse.Variant.values()) {
					if (!h.name().equalsIgnoreCase(v)) continue;
					this.variant = h;
					break;
				}
				if (this.variant == null) DebugHandler.debugBadEnumValue(Horse.Variant.class, v);
			}
			if (a != null) this.armor = Util.getItemStackFromString(a);
		}
		
		this.strAlreadyMounted = getConfigString("str-already-mounted", "You are already mounted!");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (player.getVehicle() != null) {
				sendMessage(this.strAlreadyMounted, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			Entity entity = player.getWorld().spawnEntity(player.getLocation(), this.type);
			MagicSpells.getVolatileCodeHandler().setGravity(entity, this.gravity);
			if (this.type == EntityType.HORSE) {
				((Horse)entity).setTamed(true);
				((Horse)entity).setOwner(player);
				((Horse)entity).setJumpStrength(2d);
				((Horse)entity).setAdult();
				if (this.color != null) {
					((Horse)entity).setColor(color);
				} else {
					((Horse)entity).setColor(Horse.Color.values()[this.random.nextInt(Horse.Color.values().length)]);
				}
				if (this.style != null) {
					((Horse)entity).setStyle(this.style);
				} else {
					((Horse)entity).setStyle(Horse.Style.values()[this.random.nextInt(Horse.Style.values().length)]);
				}
				if (this.variant != null) {
					((Horse)entity).setVariant(this.variant);
				} else {
					((Horse)entity).setVariant(Horse.Variant.values()[this.random.nextInt(Horse.Variant.values().length)]);
				}
				((Horse)entity).setTamed(true);
				((Horse)entity).setOwner(player);
				((Horse)entity).getInventory().setSaddle(new ItemStack(Material.SADDLE));
				if (this.armor != null) ((Horse)entity).getInventory().setArmor(this.armor);
			}
			entity.setPassenger(player);
			playSpellEffects(EffectPosition.CASTER, player);
			this.mounted.put(player.getName(), entity.getEntityId());
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler
	void onDamage(EntityDamageEvent event) {
		if (this.mounted.containsValue(event.getEntity().getEntityId())) event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onDismount(EntityDismountEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		if (!this.mounted.containsKey(player.getName())) return;
		this.mounted.remove(player.getName());
		event.getDismounted().remove();
		playSpellEffects(EffectPosition.DISABLED, player);
	}
	
	@EventHandler
	void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		String playerName = player.getName();
		if (!this.mounted.containsKey(playerName)) return;
		if (player.getVehicle() == null) return;
		this.mounted.remove(playerName);
		Entity vehicle = player.getVehicle();
		vehicle.eject();
		vehicle.remove();
	}
	
	@EventHandler
	void onQuit(PlayerQuitEvent event) {
		if (!this.mounted.containsKey(event.getPlayer().getName())) return;
		if (event.getPlayer().getVehicle() == null) return;
		this.mounted.remove(event.getPlayer().getName());
		Entity vehicle = event.getPlayer().getVehicle();
		vehicle.eject();
		vehicle.remove();
	}
	
	@Override
	public void turnOff() {
		for (String playerName : this.mounted.keySet()) {
			Player player = Bukkit.getPlayerExact(playerName);
			if (player == null) continue;
			if (player.getVehicle() == null) continue;
			player.getVehicle().eject();
		}
		this.mounted.clear();
	}

}
