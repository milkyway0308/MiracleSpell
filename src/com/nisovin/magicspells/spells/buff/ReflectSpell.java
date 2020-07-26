package com.nisovin.magicspells.spells.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellPreImpactEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ReflectSpell extends BuffSpell {

	private HashMap<String, Float> reflectors;
	private HashSet<String> shieldBreakerNames;
	private HashSet<String> delayedReflectionSpells;

	String strShieldBrokenSelf;
	String strShieldBrokenTarget;
	float reflectedSpellPowerMultiplier;
	boolean spellPowerAffectsReflectedPower;
	boolean delayedReflectionSpellsUsePayloadShieldBreaker;

	public ReflectSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		reflectors = new HashMap<>();
		shieldBreakerNames = new HashSet<>();
		delayedReflectionSpells = new HashSet<>();
		shieldBreakerNames.addAll(getConfigStringList("shield-breakers", new ArrayList<>()));
		delayedReflectionSpells.addAll(getConfigStringList("delayed-reflection-spells", new ArrayList<>()));
		reflectedSpellPowerMultiplier = (float) getConfigDouble("reflected-spell-power-multiplier", 1.0);
		spellPowerAffectsReflectedPower = getConfigBoolean("spell-power-affects-reflected-power", false);
		delayedReflectionSpellsUsePayloadShieldBreaker = getConfigBoolean("delayed-reflection-spells-use-payload-shield-breaker", true);
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		reflectors.put(player.getName(), power);
		return true;
	}

	@EventHandler
	public void onSpellTarget(SpellTargetEvent event) {
		if (event.isCancelled()) return;
		if (event.getTarget() instanceof Player) {
			Player target = (Player)event.getTarget();
			if (isActive(target)) {
				float power = reflectors.get(target.getName());
				if (shieldBreakerNames != null && shieldBreakerNames.contains(event.getSpell().getInternalName())) {
					turnOffBuff(target);
					return;
				}
				if (delayedReflectionSpells != null && delayedReflectionSpells.contains(event.getSpell().getInternalName())) {
					// Let the delayed reflection spells target the reflector so the animations run
					// It will get reflected later
					return;
				}
				boolean ok = chargeUseCost(target);
				if (ok) {
					event.setTarget(event.getCaster());
					event.setPower(event.getPower() * reflectedSpellPowerMultiplier * (spellPowerAffectsReflectedPower ? power : 1));
					addUse(target);
				}
			}
		}
	}

	@EventHandler
	public void onSpellPreImpact(SpellPreImpactEvent event) {
		if (event == null) {
			if (DebugHandler.isNullCheckEnabled()) {
				NullPointerException e = new NullPointerException("SpellPreImpactEvent was null!");
				e.fillInStackTrace();
				DebugHandler.nullCheck(e);
			}
			return;
		}
		if (event.getTarget() == null) {
			MagicSpells.plugin.getLogger().warning("Spell preimpact event had a null target, the spell cannot be reflected.");
			if (DebugHandler.isNullCheckEnabled()) {
				NullPointerException e = new NullPointerException("Spell preimpact event had a null target");
				e.fillInStackTrace();
				DebugHandler.nullCheck(e);
			}
			return;
		}
		if (event.getCaster() == null) {
			if (DebugHandler.isNullCheckEnabled()) {
				NullPointerException e = new NullPointerException("SpellPreImpactEvent had a null caster!");
				e.fillInStackTrace();
				DebugHandler.nullCheck(e);
			}
			return;
		}
		if (event.getTarget() instanceof Player) {
			Player target = (Player)event.getTarget();
			if (isActive(target)) {
				if (delayedReflectionSpellsUsePayloadShieldBreaker && (event.getSpell() != null && shieldBreakerNames.contains(event.getSpell().getInternalName()))) {
					turnOffBuff(target);
					return;
				}
				event.setRedirected(true);
				float powerMultiplier = 1.0F;
				powerMultiplier *= reflectedSpellPowerMultiplier * (spellPowerAffectsReflectedPower ? (reflectors.get(target) == null ? 1.0: reflectors.get(target)) : 1.0);
				event.setPower(event.getPower() * powerMultiplier);
				addUse(target);
			}
		}
	}

	@Override
	public void turnOffBuff(Player player) {
		reflectors.remove(player.getName());
	}

	@Override
	protected void turnOff() {
		reflectors.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return reflectors.containsKey(player.getName());
	}

}
