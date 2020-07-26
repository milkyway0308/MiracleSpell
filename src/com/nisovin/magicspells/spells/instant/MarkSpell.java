package com.nisovin.magicspells.spells.instant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;

import com.nisovin.magicspells.spells.TargetedLocationSpell;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.MagicLocation;

public class MarkSpell extends InstantSpell implements TargetedLocationSpell {
	
	private boolean permanentMarks;
	private boolean useAsRespawnLocation;
	
	private HashMap<String,MagicLocation> marks;

	private boolean enableDefaultMarks;
	private MagicLocation defaultMark = null;
	
	
	public MarkSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.permanentMarks = getConfigBoolean("permanent-marks", true);
		this.useAsRespawnLocation = getConfigBoolean("use-as-respawn-location", false);
		
		this.marks = new HashMap<>();
		
		this.enableDefaultMarks = getConfigBoolean("enable-default-marks", false);
		
		if (this.enableDefaultMarks) {
			String s = getConfigString("default-mark", "world,0,0,0");
			try {
				String[] split = s.split(",");
				String world = split[0];
				double x = Double.parseDouble(split[1]);
				double y = Double.parseDouble(split[2]);
				double z = Double.parseDouble(split[3]);
				float yaw = 0;
				float pitch = 0;
				if (split.length > 4) yaw = Float.parseFloat(split[4]);
				if (split.length > 5) pitch = Float.parseFloat(split[5]);
				this.defaultMark = new MagicLocation(world, x, y, z, yaw, pitch);
			} catch (Exception e) {
				MagicSpells.error("Invalid default mark on MarkSpell '" + spellName + '\'');
			}
		}
		
		if (this.permanentMarks) loadMarks();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			this.marks.put(getPlayerKey(player), new MagicLocation(player.getLocation()));
			if (this.permanentMarks) saveMarks();
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!this.permanentMarks) this.marks.remove(getPlayerKey(event.getPlayer()));
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!this.useAsRespawnLocation) return;
		MagicLocation loc = this.marks.get(getPlayerKey(event.getPlayer()));
		if (loc != null) {
			event.setRespawnLocation(loc.getLocation());
		} else if (this.enableDefaultMarks && this.defaultMark != null) {
			event.setRespawnLocation(this.defaultMark.getLocation());
		}
	}
	
	public HashMap<String,MagicLocation> getMarks() {
		return this.marks;
	}
	
	public void setMarks(HashMap<String,MagicLocation> newMarks) {
		this.marks = newMarks;
		if (this.permanentMarks) saveMarks();
	}
	
	private void loadMarks() {
		try {
			Scanner scanner = new Scanner(new File(MagicSpells.plugin.getDataFolder(), "marks-" + this.internalName + ".txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.isEmpty()) {
					try {
						String[] data = line.split(":");
						MagicLocation loc = new MagicLocation(data[1], Double.parseDouble(data[2]), Double.parseDouble(data[3]), Double.parseDouble(data[4]), Float.parseFloat(data[5]), Float.parseFloat(data[6]));
						this.marks.put(data[0].toLowerCase(), loc);
					} catch (Exception e) {
						MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Failed to load mark: " + line);
					}
				}
			}
			scanner.close();
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
		}
	}
	
	private void saveMarks() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(MagicSpells.plugin.getDataFolder(), "marks-" + this.internalName + ".txt"), false));
			for (String name : this.marks.keySet()) {
				MagicLocation loc = this.marks.get(name);
				writer.append(name + ':' + loc.getWorld() + ':' + loc.getX() + ':' + loc.getY() + ':' + loc.getZ() + ':' + loc.getYaw() + ':' + loc.getPitch());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Error saving marks");
		}		
	}
	
	public String getPlayerKey(Player player) {
		if (player == null) return null;
		return player.getName().toLowerCase();
	}
	
	public boolean usesDefaultMark() {
		return this.enableDefaultMarks;
	}
	
	public Location getEffectiveMark(Player player) {
		MagicLocation m = this.marks.get(getPlayerKey(player));
		if (m == null) {
			if (this.enableDefaultMarks) return this.defaultMark.getLocation();
			return null;
		}
		return m.getLocation();
	}
	
	public Location getEffectiveMark(String player) {
		MagicLocation m = this.marks.get(player.toLowerCase());
		if (m == null) {
			if (this.enableDefaultMarks) return this.defaultMark.getLocation();
			return null;
		}
		return m.getLocation();
	}
	
	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		this.marks.put(getPlayerKey(caster), new MagicLocation(target));
		if (caster != null) playSpellEffects(caster, target);
		return true;
	}
	
	@Override
	public boolean castAtLocation(Location target, float power) {
		return false;
	}
	
}
