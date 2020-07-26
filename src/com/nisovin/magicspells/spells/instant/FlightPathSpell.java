package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class FlightPathSpell extends InstantSpell {

	static FlightHandler flightHandler;

	float targetX;
	float targetY;
	float targetZ;
	int cruisingAltitude;
	float speed;
	EntityType mount;
	
	public FlightPathSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.targetX = getConfigFloat("x", 0);
		this.targetY = getConfigFloat("y", 70);
		this.targetZ = getConfigFloat("z", 0);
		this.cruisingAltitude = getConfigInt("cruising-altitude", 150);
		this.speed = getConfigFloat("speed", 1.5F);
		this.mount = Util.getEntityType(getConfigString("mount", ""));
		
		if (flightHandler == null) flightHandler = new FlightHandler();
		
	}
	
	@Override
	public void initialize() {
		flightHandler.init();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			ActiveFlight flight = new ActiveFlight(player, this.mount, this);
			flightHandler.addFlight(flight);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public void turnOff() {
		if (flightHandler != null) {
			flightHandler.turnOff();
			flightHandler = null;
		}
	}	
	
	class FlightHandler implements Runnable, Listener {

		boolean inited = false;
		
		Map<String, ActiveFlight> flights = new HashMap<>();
		int task = -1;
		
		public void addFlight(ActiveFlight flight) {
			this.flights.put(flight.player.getName(), flight);
			flight.start();
			if (this.task < 0) this.task = MagicSpells.scheduleRepeatingTask(this, 0, 5);
		}
		
		void init() {
			if (this.inited) return;
			this.inited = true;
			MagicSpells.registerEvents(this);
		}

		void cancel(Player player) {
			ActiveFlight flight = this.flights.remove(player.getName());
			if (flight != null) flight.cancel();
		}
		
		void turnOff() {
			for (ActiveFlight flight : this.flights.values()) {
				flight.cancel();
			}
			MagicSpells.cancelTask(this.task);
			this.flights.clear();
		}
		
		@EventHandler
		void onTeleport(PlayerTeleportEvent event) {
			cancel(event.getPlayer());
		}
		
		@EventHandler
		void onPlayerDeath(PlayerDeathEvent event) {
			cancel(event.getEntity());
		}
		
		@EventHandler
		void onQuit(PlayerQuitEvent event) {
			cancel(event.getPlayer());
		}
		
		@Override
		public void run() {
			Iterator<ActiveFlight> iter = this.flights.values().iterator();
			while (iter.hasNext()) {
				ActiveFlight flight = iter.next();
				if (flight.isDone()) {
					iter.remove();
				} else {
					flight.fly();
				}
			}
			if (this.flights.isEmpty()) {
				MagicSpells.cancelTask(this.task);
				this.task = -1;
			}
		}
		
	}
	
	class ActiveFlight {
		
		Player player;
		EntityType mountType;
		Entity mountActive;
		Entity entityToPush;
		FlightPathSpell spell;
		FlightState state;
		boolean wasFlyingAllowed;
		boolean wasFlying;
		
		Location lastLocation;
		int sameLocCount = 0;
		
		public ActiveFlight(Player caster, EntityType entityMountType, FlightPathSpell flightPathSpell) {
			this.player = caster;
			this.mountType = entityMountType;
			this.spell = flightPathSpell;
			this.state = FlightState.TAKE_OFF;
			this.wasFlyingAllowed = caster.getAllowFlight();
			this.wasFlying = caster.isFlying();
			this.lastLocation = caster.getLocation();
		}
		
		void start() {
			this.player.setAllowFlight(true);
			this.spell.playSpellEffects(EffectPosition.CASTER, this.player);
			if (this.mountType == null) {
				this.entityToPush = this.player;
			} else {
				this.mountActive = this.player.getWorld().spawnEntity(this.player.getLocation(), this.mountType);
				this.entityToPush = this.mountActive;
				if (this.player.getVehicle() != null) this.player.getVehicle().eject();
				this.mountActive.setPassenger(this.player);
			}
		}
		
		void fly() {
			if (this.state == FlightState.DONE) return;
			
			// Check for stuck
			if (this.player.getLocation().distanceSquared(this.lastLocation) < 0.4) {
				this.sameLocCount++;
			}
			if (this.sameLocCount > 12) {
				MagicSpells.error("Flight stuck '" + this.spell.getInternalName() + "' at " + this.player.getLocation());
				cancel();
				return;
			}
			this.lastLocation = this.player.getLocation();
			
			// Do flight
			if (this.state == FlightState.TAKE_OFF) {
				this.player.setFlying(false);
				double y = this.entityToPush.getLocation().getY();
				if (y >= this.spell.cruisingAltitude) {
					this.entityToPush.setVelocity(new Vector(0, 0, 0));
					this.state = FlightState.CRUISING;
				} else {
					this.entityToPush.setVelocity(new Vector(0, 2, 0));
				}
			} else if (this.state == FlightState.CRUISING) {
				this.player.setFlying(true);
				double x = this.entityToPush.getLocation().getX();
				double z = this.entityToPush.getLocation().getZ();
				if (this.spell.targetX - 1 <= x && x <= this.spell.targetX + 1 && this.spell.targetZ - 1 <= z && z <= this.spell.targetZ + 1) {
					this.entityToPush.setVelocity(new Vector(0, 0, 0));
					this.state = FlightState.LANDING;
				} else {
					Vector t = new Vector(this.spell.targetX, this.spell.cruisingAltitude, this.spell.targetZ);
					Vector v = t.subtract(this.entityToPush.getLocation().toVector());
					double len = v.lengthSquared();
					v.normalize().multiply(len > 25 ? this.spell.speed : 0.3);
					this.entityToPush.setVelocity(v);
				}
			} else if (this.state == FlightState.LANDING) {
				this.player.setFlying(false);
				Location l = this.entityToPush.getLocation();
				if (l.getBlock().getType() != Material.AIR || l.subtract(0, 1, 0).getBlock().getType() != Material.AIR || l.subtract(0, 2, 0).getBlock().getType() != Material.AIR) {
					this.player.setFallDistance(0f);
					cancel();
				} else {
					this.entityToPush.setVelocity(new Vector(0, -1, 0));
					this.player.setFallDistance(0f);
				}
			}
			
			this.spell.playSpellEffects(EffectPosition.SPECIAL, this.player);
		}
		
		void cancel() {
			if (this.state != FlightState.DONE) {
				this.state = FlightState.DONE;
				this.player.setFlying(this.wasFlying);
				this.player.setAllowFlight(this.wasFlyingAllowed);
				if (this.mountActive != null) {
					this.mountActive.eject();
					this.mountActive.remove();
				}
				this.spell.playSpellEffects(EffectPosition.DELAYED, this.player);
				
				this.player = null;
				this.mountActive = null;
				this.entityToPush = null;
				this.spell = null;
			}
		}
		
		boolean isDone() {
			return this.state == FlightState.DONE;
		}
		
	}
	
	enum FlightState {
		
		TAKE_OFF,
		CRUISING,
		LANDING,
		DONE
		
	}

}
