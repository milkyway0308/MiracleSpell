package com.nisovin.magicspells.castmodifiers.conditions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.Util;

public class EntityTypeCondition extends Condition {

	boolean player = false;
	boolean monster = false;
	boolean animal = false;
	Set<EntityType> types = new HashSet<>();
	
	@Override
	public boolean setVar(String var) {
		String[] vars = var.replace(" ", "").split(",");
		for (String v : vars) {
			if (v.equalsIgnoreCase("player")) {
				player = true;
			} else if (v.equalsIgnoreCase("monster")) {
				monster = true;
			} else if (v.equalsIgnoreCase("animal")) {
				animal = true;
			} else {
				EntityType type = Util.getEntityType(v);
				if (type != null) {
					types.add(type);
				}
			}
		}
		return player || monster || animal || !types.isEmpty();
	}

	@Override
	public boolean check(Player player) {
		return check(player, player);
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		if (this.player && target instanceof Player) return true;
		if (monster && target instanceof Monster) return true;
		if (animal && target instanceof Animals) return true;
		return types.contains(target.getType());
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
