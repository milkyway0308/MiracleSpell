package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;

import java.util.regex.Pattern;

public class GateSpell extends InstantSpell {
	
	private String world;
	private String coords;
	private String strGateFailed;

	private static final Pattern COORDINATE_PATTERN = Pattern.compile("^-?[0-9]+,[0-9]+,-?[0-9]+(,-?[0-9.]+,-?[0-9.]+)?$");
	
	public GateSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		world = getConfigString("world", "CURRENT");
		coords = getConfigString("coordinates", "SPAWN");
		strGateFailed = getConfigString("str-gate-failed", "Unable to teleport.");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// Get world
			World effectiveWorld;
			if (this.world.equals("CURRENT")) {
				effectiveWorld = player.getWorld();
			} else if (this.world.equals("DEFAULT")) {
				effectiveWorld = Bukkit.getServer().getWorlds().get(0);
			} else {
				effectiveWorld = Bukkit.getServer().getWorld(this.world);
			}
			if (effectiveWorld == null) {
				// Fail -- no world
				MagicSpells.error(this.name + ": world " + this.world + " does not exist");
				sendMessage(this.strGateFailed, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get location
			Location location;
			this.coords = this.coords.replace(" ", "");
			if (RegexUtil.matches(COORDINATE_PATTERN, this.coords)) {
				String[] c = this.coords.split(",");
				int x = Integer.parseInt(c[0]);
				int y = Integer.parseInt(c[1]);
				int z = Integer.parseInt(c[2]);
				float yaw = 0;
				float pitch = 0;
				if (c.length > 3) {
					yaw = Float.parseFloat(c[3]);
					pitch = Float.parseFloat(c[4]);
				}
				location = new Location(effectiveWorld, x, y, z, yaw, pitch);
			} else if (this.coords.equals("SPAWN")) {
				location = effectiveWorld.getSpawnLocation();
				location = new Location(effectiveWorld, location.getX(), effectiveWorld.getHighestBlockYAt(location), location.getZ());
			} else if (this.coords.equals("EXACTSPAWN")) {
				location = effectiveWorld.getSpawnLocation();
			} else if (this.coords.equals("CURRENT")) {
				Location l = player.getLocation();
				location = new Location(effectiveWorld, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getYaw(), l.getPitch());
			} else {
				// Fail -- no location
				MagicSpells.error(this.name + ": " + this.coords + " is not a valid location");
				sendMessage(this.strGateFailed, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			location.setX(location.getX() + .5);
			location.setZ(location.getZ() + .5);
			MagicSpells.debug(3, "Gate location: " + location.toString());
			
			// Check for landing point
			Block b = location.getBlock();
			if (!BlockUtils.isPathable(b) || !BlockUtils.isPathable(b.getRelative(0, 1, 0))) {
				// Fail -- blocked
				MagicSpells.error(this.name + ": landing spot blocked");
				sendMessage(this.strGateFailed, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Teleport caster
			Location from = player.getLocation();
			Location to = b.getLocation();
			boolean teleported = player.teleport(location);
			if (teleported) {
				playSpellEffects(EffectPosition.CASTER, from);
				playSpellEffects(EffectPosition.TARGET, to);
			} else {
				// Fail - teleport blocked
				MagicSpells.error(this.name + ": teleport prevented!");
				sendMessage(this.strGateFailed, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
