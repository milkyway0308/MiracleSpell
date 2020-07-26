package com.nisovin.magicspells.castmodifiers.conditions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.Util;

public class TargetingCondition extends Condition {

	Set<EntityType> allowedTypes;
	boolean anyType = false;
	boolean targetingCaster = false;
	
	@Override
	public boolean setVar(String var) {
		if (var == null || var.isEmpty()) {
			anyType = true;
			return true;
		}
		if (var.equalsIgnoreCase("caster")) {
			targetingCaster = true;
			return true;
		}
		
		String[] entityTypes = var.split(",");
		allowedTypes = new HashSet<>();
		for (String type: entityTypes) {
			EntityType entityType = Util.getEntityType(type);
			if (entityType != null) {
				allowedTypes.add(entityType);
			}
		}
		return !allowedTypes.isEmpty();
	}

	@Override
	public boolean check(Player player) {
		return false;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Creature) {
			LivingEntity creatureTarget = ((Creature) target).getTarget();
			if (creatureTarget != null) {
				if (anyType) return true;
				if (targetingCaster && creatureTarget.equals(player)) return true;
				if (allowedTypes.contains(creatureTarget.getType())) return true;
			}
		}
		return false;
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
