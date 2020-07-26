package com.nisovin.magicspells.events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class MagicSpellsEntityDamageByEntityEvent extends EntityDamageByEntityEvent implements IMagicSpellsCompatEvent {
	
	public MagicSpellsEntityDamageByEntityEvent(Entity damager, Entity damagee, DamageCause cause, double damage) {
		//super(damager, damagee, cause, damage);
		super(damager, damagee, cause, getModTemplate(damage), getModifierFunctionTemplate(0D));
	}
	
	private static Map<DamageModifier, Double> getModTemplate(double baseDamage) {
		return new HashMap<>(ImmutableMap.of(DamageModifier.BASE, baseDamage));
	}
	
	private static Map<DamageModifier, Function<Double, Double>> getModifierFunctionTemplate(final double baseDamage) {
		return new HashMap<>(ImmutableMap.of(DamageModifier.BASE, getConstantFunction(baseDamage)));
	}
	
	private static Function<Double, Double> getConstantFunction(final double value) {
		return new Function<Double, Double>() {
			
			@Override
			public Double apply(Double arg0) {
				return value;
			}
			
		};
	}
	
}
