package com.nisovin.magicspells.util;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.expression.ValueResolver;

public class RandomColorHexCodeDecimalResolver extends ValueResolver {

	private static Random rand = new Random();
	
	@Override
	public Number resolveValue(String playerName, Player player, Location loc1, Location loc2) {
		String ret = "";
		for (int i = 0; i < 6; i++) {
			ret += Integer.toHexString(rand.nextInt(16));
		}
		return Integer.parseInt(ret, 16);
	}

}
