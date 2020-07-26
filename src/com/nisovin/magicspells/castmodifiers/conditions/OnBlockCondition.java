package com.nisovin.magicspells.castmodifiers.conditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.materials.MagicMaterial;

public class OnBlockCondition extends Condition {

	Set<Material> types;
	List<MagicMaterial> mats;
	MagicMaterial mat;

	@Override
	public boolean setVar(String var) {
		if (var.contains(",")) {
			types = new HashSet<>();
			mats = new ArrayList<>();
			String[] split = var.split(",");
			for (String s : split) {
				MagicMaterial mat = MagicSpells.getItemNameResolver().resolveBlock(s);
				if (mat == null) return false;
				types.add(mat.getMaterial());
				mats.add(mat);
			}
			return true;
		}
		mat = MagicSpells.getItemNameResolver().resolveBlock(var);
		return mat != null;
	}

	@Override
	public boolean check(Player player) {
		return check(player, player);
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		Block block = target.getLocation().subtract(0, 1, 0).getBlock();
		if (mat != null) return mat.equals(block);
		if (types.contains(block.getType())) {
			for (MagicMaterial m : mats) {
				if (m.equals(block)) return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
