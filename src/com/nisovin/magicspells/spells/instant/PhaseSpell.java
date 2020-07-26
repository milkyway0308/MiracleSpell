package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class PhaseSpell extends InstantSpell {
	
	private int range;
	private int maxDistance;
	private List<MagicMaterial> allowedPassThru;
	private String strCantPhase;
	
	public PhaseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.range = getConfigInt("range", 5);
		this.maxDistance = getConfigInt("max-distance", 15);
		List<String> passThru = getConfigStringList("allowed-pass-thru-blocks", null);
		this.allowedPassThru = new ArrayList<>();
		for (String s : passThru) {
			MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
			if (m == null) continue;
			this.allowedPassThru.add(m);
		}
		this.strCantPhase = getConfigString("str-cant-phase", "Unable to find place to phase to.");
		
		if (this.allowedPassThru.isEmpty()) this.allowedPassThru = null;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int range = Math.round(this.range * power);
			int distance = Math.round(this.maxDistance * power);
			
			BlockIterator iter;
			try {
				iter = new BlockIterator(player, distance << 1);
			} catch (IllegalStateException e) {
				sendMessage(strCantPhase, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			Block start = null;
			int i = 0;
			Location location = null;
			
			// Get wall block
			while (start == null && i++ < range << 1 && iter.hasNext()) {
				Block b = iter.next();
				if (b.getType() == Material.AIR) continue;
				if (player.getLocation().distanceSquared(b.getLocation()) < range * range) {
					start = b;
					break;
				}
			}
			
			// Find landing spot
			if (start != null) {
				if (this.allowedPassThru != null && !canPassThru(start)) {
					// Can't phase through the block
					location = null;
				} else {
					// Get next empty space
					Block end = null;
					while (end == null && i++ < distance << 1 && iter.hasNext()) {
						Block b = iter.next();
						// Check for suitable landing location
						if (b.getType() == Material.AIR && b.getRelative(0, 1, 0).getType() == Material.AIR && player.getLocation().distanceSquared(b.getLocation()) < distance * distance) {
							location = b.getLocation();
							break;
						}
						// Check for invalid pass-thru block
						if (this.allowedPassThru != null && !canPassThru(b)) {
							location = null;
							break;
						}
					}
				}
			}
			
			// Check for fail
			if (location == null) {
				// No location to tp to
				sendMessage(this.strCantPhase, player, args);
				//fizzle(player);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Teleport
			location.setX(location.getX() + .5);
			location.setZ(location.getZ() + .5);
			location.setPitch(player.getLocation().getPitch());
			location.setYaw(player.getLocation().getYaw());
			playSpellEffects(EffectPosition.CASTER, player.getLocation());
			playSpellEffects(EffectPosition.TARGET, location);
			player.teleport(location);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean canPassThru(Block block) {
		if (this.allowedPassThru == null) return true;
		for (MagicMaterial mat : this.allowedPassThru) {
			if (mat.equals(block)) return true;
		}
		return false;
	}

}
