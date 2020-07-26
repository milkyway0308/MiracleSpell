package com.nisovin.magicspells.spells.instant;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.LocationUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

// Advanced perm is for teleporting to other player's recall points
public class RecallSpell extends InstantSpell implements TargetedEntitySpell {
	
	private String markSpellName;
	private boolean allowCrossWorld;
	private double maxRange;
	private boolean useBedLocation;
	private String strNoMark;
	private String strOtherWorld;
	private String strTooFar;
	private String strRecallFailed;
	
	private MarkSpell markSpell;

	public RecallSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.markSpellName = getConfigString("mark-spell", "mark");
		this.allowCrossWorld = getConfigBoolean("allow-cross-world", true);
		this.maxRange = getConfigDouble("max-range", 0);
		this.useBedLocation = getConfigBoolean("use-bed-location", false);
		this.strNoMark = getConfigString("str-no-mark", "You have no mark to recall to.");
		this.strOtherWorld = getConfigString("str-other-world", "Your mark is in another world.");
		this.strTooFar = getConfigString("str-too-far", "You mark is too far away.");
		this.strRecallFailed = getConfigString("str-recall-failed", "Could not recall.");
	}
	
	@Override
	public void initialize() {
		super.initialize();
		Spell spell = MagicSpells.getSpellByInternalName(this.markSpellName);
		if (spell instanceof MarkSpell) {
			this.markSpell = (MarkSpell)spell;
		} else {
			MagicSpells.error("Failed to get marks list for '" + this.internalName + "' spell");
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location markLocation = null;
			if (args != null && args.length == 1 && player.hasPermission("magicspells.advanced." + this.internalName)) {
				Player target = PlayerNameUtils.getPlayer(args[0]);				
				if (this.useBedLocation) {
					if (target != null) markLocation = target.getBedSpawnLocation();
				} else if (this.markSpell != null) {
					Location loc = this.markSpell.getEffectiveMark(target != null ? target.getName().toLowerCase() : args[0].toLowerCase());
					if (loc != null) markLocation = loc;
				}
			} else {
				markLocation = getRecallLocation(player);
			}
			if (markLocation == null) {
				sendMessage(this.strNoMark, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (!this.allowCrossWorld && !LocationUtil.isSameWorld(markLocation, player.getLocation())) {
				// Can't cross worlds
				sendMessage(this.strOtherWorld, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			if (this.maxRange > 0 && markLocation.toVector().distanceSquared(player.getLocation().toVector()) > this.maxRange * this.maxRange) {
				// Too far
				sendMessage(this.strTooFar, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// All good!
			Location from = player.getLocation();
			boolean teleported = player.teleport(markLocation);
			if (teleported) {
				playSpellEffects(EffectPosition.CASTER, from);
				playSpellEffects(EffectPosition.TARGET, markLocation);
			} else {
				// Fail -- teleport prevented
				MagicSpells.error("Recall teleport blocked for " + player.getName());
				sendMessage(this.strRecallFailed, player, args);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	Location getRecallLocation(Player caster) {
		if (this.useBedLocation) return caster.getBedSpawnLocation();
		if (this.markSpell == null) return null;
		return this.markSpell.getEffectiveMark(caster);
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		Location mark = getRecallLocation(caster);
		if (mark == null) return false;
		target.teleport(mark);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return false;
	}

}
