package com.nisovin.magicspells.util;

import java.util.HashSet;
import java.util.Set;

public class BooleanUtils {

	private static Set<String> yesStrings;
	static {
		yesStrings = new HashSet<>();
		yesStrings.add("yes");
		yesStrings.add("true");
	}
	
	public static boolean isYes(String toCheck) {
		return yesStrings.contains(toCheck.trim().toLowerCase());
	}
	
}
