package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.util.compat.CompatBasics;
import org.bukkit.EntityEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import com.nisovin.magicspells.spells.SpellDamageSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.expression.Expression;

/**
 * public class PainSpell extends {@link TargetedSpell} implements {@link TargetedEntitySpell}, {@link SpellDamageSpell}
 * Configuration fields:
 * <ul>
 * <li>damage: 4<br>Affected by spell power</li>
 * <li>spell-damage-type: ""</li>
 * <li>ignore-armor: false</li>
 * <li>check-plugins: true</li>
 * <li>damage-type: "ENTITY_ATTACK"</li>
 * </ul>
 */
public class PainSpell extends TargetedSpell implements TargetedEntitySpell, SpellDamageSpell {

	private Expression damageExpression;
	private double damage;
	private String spellDamageType;
	private boolean ignoreArmor;
	private boolean checkPlugins;
	private DamageCause damageType;
	private boolean avoidDamageModification;
	private boolean tryAvoidingAntiCheatPlugins;
	
	public PainSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		damage = getConfigFloat("damage", 4);
		
		String damageExpressionString = getConfigString("damage-expression", null);
		if (damageExpressionString == null) {
			damageExpression = new Expression("0 + " + damage);
		} else {
			damageExpression = new Expression(damageExpressionString);
		}
		
		spellDamageType = getConfigString("spell-damage-type", "");
		ignoreArmor = getConfigBoolean("ignore-armor", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
		
		tryAvoidingAntiCheatPlugins = getConfigBoolean("try-avoiding-anticheat-plugins", false);
		
		String type = getConfigString("damage-type", "ENTITY_ATTACK");
		for (DamageCause cause : DamageCause.values()) {
			if (cause.name().equalsIgnoreCase(type)) {
				damageType = cause;
				break;
			}
		}
		if (damageType == null) {
			DebugHandler.debugBadEnumValue(DamageCause.class, type);
			damageType = DamageCause.ENTITY_ATTACK;
		}
		avoidDamageModification = getConfigBoolean("avoid-damage-modification", false);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> target = getTargetedEntity(player, power);
			if (target == null) {
				// Fail -- no target
				return noTarget(player);
			}
			
			boolean done = CompatBasics.exemptAction(() -> causePain(player, target.getTarget(), target.getPower()), player, CompatBasics.activeExemptionAssistant.getPainExemptions());
			if (!done) return noTarget(player);
			
			sendMessages(player, target.getTarget());
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public String getSpellDamageType() {
		return spellDamageType;
	}
	
	private boolean causePain(Player player, LivingEntity target, float power) {
		if (target.isDead()) return false;
		double resolvedValue = damageExpression.resolveValue(null, player, player.getLocation(), target.getLocation()).doubleValue();
		MagicSpells.log(MagicSpells.DEVELOPER_DEBUG_LEVEL, "Damage resolver resolved value of " + resolvedValue);
		double localDamage = resolvedValue * power;
		//double dam = damage * power;
		if (target instanceof Player && checkPlugins) {
			// Handle the event myself so I can detect cancellation properly
			MagicSpellsEntityDamageByEntityEvent event = new MagicSpellsEntityDamageByEntityEvent(player, target, damageType, localDamage);
			EventUtil.call(event);
			if (event.isCancelled()) return false;
			if (!avoidDamageModification) localDamage = event.getDamage();
			target.setLastDamageCause(event);
		}
		//Bukkit.getPluginManager().callEvent(new SpellApplyDamageEvent(this, player, target, localDamage, DamageCause.MAGIC));
		SpellApplyDamageEvent event = new SpellApplyDamageEvent(this, player, target, localDamage, damageType, spellDamageType);
		EventUtil.call(event);
		localDamage = event.getFinalDamage();
		if (ignoreArmor) {
			double health = target.getHealth();
			if (health > target.getMaxHealth()) health = target.getMaxHealth();
			health = health - localDamage;
			if (health < 0) health = 0;
			if (health > target.getMaxHealth()) health = target.getMaxHealth();
			if (health == 0) MagicSpells.getVolatileCodeHandler().setKiller(target, player);
			target.setHealth(health);
			target.playEffect(EntityEffect.HURT);
		} else {
			if (tryAvoidingAntiCheatPlugins) {
				target.damage(localDamage);
			} else {
				target.damage(localDamage, player);
			}
		}
		playSpellEffects(player, target);
		return true;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) return false;
		return causePain(caster, target, power);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!validTargetList.canTarget(target)) return false;
		return causePain(null, target, power);
	}

}
