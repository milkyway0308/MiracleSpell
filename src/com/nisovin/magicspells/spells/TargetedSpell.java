package com.nisovin.magicspells.spells;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nisovin.magicspells.util.TxtUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public abstract class TargetedSpell extends InstantSpell {

	@ConfigData(field="always-activate", dataType="boolean", defaultValue="false")
	protected boolean alwaysActivate;
	
	@ConfigData(field="play-fizzle-sound", dataType="boolean", defaultValue="false")
	protected boolean playFizzleSound;
	
	@ConfigData(field="target-self", dataType="boolean", defaultValue="false")
	protected boolean targetSelf;
	
	@ConfigData(field="spell-on-fail", dataType="String", defaultValue="null")
	protected String spellNameOnFail;
	
	protected Subspell spellOnFail;
	
	@ConfigData(field="str-cast-target", dataType="String", defaultValue="null")
	protected String strCastTarget;
	
	@ConfigData(field="str-no-target", dataType="String", defaultValue="null")
	protected String strNoTarget;

	public TargetedSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.alwaysActivate = getConfigBoolean("always-activate", false);
		this.playFizzleSound = getConfigBoolean("play-fizzle-sound", false);
		this.targetSelf = getConfigBoolean("target-self", false);
		this.spellNameOnFail = getConfigString("spell-on-fail", null);
		this.strCastTarget = getConfigString("str-cast-target", "");
		this.strNoTarget = getConfigString("str-no-target", "");
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (this.spellNameOnFail != null && !this.spellNameOnFail.isEmpty()) {
			this.spellOnFail = new Subspell(this.spellNameOnFail);
			if (!this.spellOnFail.process()) {
				this.spellOnFail = null;
				MagicSpells.error("Invalid spell-on-fail for spell " + this.internalName);
			}
		}
	}
	
	protected void sendMessages(Player caster, LivingEntity target) {
		String targetName = getTargetName(target);
		Player playerTarget = null;
		if (target instanceof Player) playerTarget = (Player)target;
		sendMessage(prepareMessage(this.strCastSelf, caster, targetName, playerTarget), caster, MagicSpells.NULL_ARGS);
		if (playerTarget != null) sendMessage(prepareMessage(this.strCastTarget, caster, targetName, playerTarget), playerTarget, MagicSpells.NULL_ARGS);
		sendMessageNear(caster, playerTarget, prepareMessage(this.strCastOthers, caster, targetName, playerTarget), this.broadcastRange, MagicSpells.NULL_ARGS);
	}
	
	private String prepareMessage(String message, Player caster, String targetName, Player playerTarget) {
		if (message != null && !message.isEmpty()) {
			message = message.replace("%a", caster.getDisplayName());
			message = message.replace("%t", targetName);
			if (playerTarget != null && MagicSpells.getVariableManager() != null && message.contains("%targetvar")) {
				Matcher matcher = chatVarTargetMatchPattern.matcher(message);
				while (matcher.find()) {
					String varText = matcher.group();
					String[] varData = varText.substring(5, varText.length() - 1).split(":");
					String val = MagicSpells.getVariableManager().getStringValue(varData[0], playerTarget);
					String sval = varData.length == 1 ? TxtUtil.getStringNumber(val, -1) : TxtUtil.getStringNumber(val, Integer.parseInt(varData[1]));
					message = message.replace(varText, sval);
				}
			}
			if (MagicSpells.getVariableManager() != null && message.contains("%castervar")) {
				Matcher matcher = chatVarCasterMatchPattern.matcher(message);
				while (matcher.find()) {
					String varText = matcher.group();
					String[] varData = varText.substring(5, varText.length() - 1).split(":");
					String val = MagicSpells.getVariableManager().getStringValue(varData[0], caster);
					String sval = varData.length == 1 ? TxtUtil.getStringNumber(val, -1) : TxtUtil.getStringNumber(val, Integer.parseInt(varData[1]));
					message = message.replace(varText, sval);
				}
			}
		}
		return message;
	}
	static private Pattern chatVarCasterMatchPattern = Pattern.compile("%castervar:[A-Za-z0-9_]+(:[0-9]+)?%", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	static private Pattern chatVarTargetMatchPattern = Pattern.compile("%targetvar:[A-Za-z0-9_]+(:[0-9]+)?%", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	protected String getTargetName(LivingEntity target) {
		if (target instanceof Player) return ((Player)target).getDisplayName();
		String name = MagicSpells.getEntityNames().get(target.getType());
		if (name != null) return name;
		return "unknown";
	}
	
	/**
	 * Checks whether two locations are within a certain distance from each other.
	 * @param loc1 The first location
	 * @param loc2 The second location
	 * @param range The maximum distance
	 * @return true if the distance is less than the range, false otherwise
	 */
	protected boolean inRange(Location loc1, Location loc2, int range) {
		return loc1.distanceSquared(loc2) < range * range;
	}
	
	/**
	 * Plays the fizzle sound if it is enabled for this spell.
	 */
	protected void fizzle(Player player) {
		if (this.playFizzleSound) player.playEffect(player.getLocation(), Effect.EXTINGUISH, null);
	}
	
	@Override
	protected TargetInfo<LivingEntity> getTargetedEntity(Player player, float power, boolean forceTargetPlayers, ValidTargetChecker checker) {
		if (this.targetSelf) return new TargetInfo<>(player, power);
		return super.getTargetedEntity(player, power, forceTargetPlayers, checker);
	}
	
	/**
	 * This should be called if a target should not be found. It sends the no target message
	 * and returns the appropriate return value.
	 * @param player the casting player
	 * @return the appropriate PostcastAction value
	 */
	protected PostCastAction noTarget(Player player) {
		return noTarget(player, this.strNoTarget);
	}
	
	/**
	 * This should be called if a target should not be found. It sends the provided message
	 * and returns the appropriate return value.
	 * @param player the casting player
	 * @param message the message to send
	 * @return
	 */
	protected PostCastAction noTarget(Player player, String message) {
		fizzle(player);
		sendMessage(message, player, MagicSpells.NULL_ARGS);
		if (this.spellOnFail != null) this.spellOnFail.cast(player, 1.0F);
		return this.alwaysActivate ? PostCastAction.NO_MESSAGES : PostCastAction.ALREADY_HANDLED;
	}
	
}
