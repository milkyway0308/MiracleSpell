package com.nisovin.magicspells.util.wrappers;

import java.lang.reflect.Method;

import de.slikey.effectlib.util.ReflectionUtils;

import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class NBTTagCompoundWrapper {
	public static Class targetClass = NBTTagCompound.class;
	
	public static Method setIntMethod;
	public static Method setByteMethod;
	public static Method setStringMethod;
	public static Method setLongMethod;
	public static Method setDoubleMethod;
	public static Method setMethod;
	public static Method hasKeyMethod;
	
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
			setIntMethod = ReflectionUtils.getMethod(targetClass, "setInt", String.class, int.class);
			setIntMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			setStringMethod = ReflectionUtils.getMethod(targetClass, "setString", String.class, String.class);
			setStringMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			setLongMethod = ReflectionUtils.getMethod(targetClass, "setLong", String.class, long.class);
			setLongMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		
		try {
			setDoubleMethod = ReflectionUtils.getMethod(targetClass, "setDouble", String.class, double.class);
			setDoubleMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			setByteMethod = ReflectionUtils.getMethod(targetClass, "setByte", String.class, byte.class);
			setByteMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			setMethod = ReflectionUtils.getMethod(targetClass, "set", String.class, NBTBase.class);
			setMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		
		try {
			hasKeyMethod = ReflectionUtils.getMethod(targetClass, "hasKey", String.class);
			hasKeyMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
	}
}
