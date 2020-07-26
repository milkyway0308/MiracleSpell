package com.nisovin.magicspells.util.wrappers;

import java.lang.reflect.Method;

import de.slikey.effectlib.util.ReflectionUtils;

import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagList;

public class NBTTagListWrapper {
	public static Class targetClass = NBTTagList.class;
	public static Method addMethod;
	
	public static Object newInstance() {
		try {
			return targetClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static {
		try {
			addMethod = ReflectionUtils.getMethod(targetClass, "add", NBTBase.class);
			addMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
}
