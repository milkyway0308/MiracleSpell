package com.nisovin.magicspells.util.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import de.slikey.effectlib.util.ReflectionUtils;

public class CraftItemStackWrapper {
	
	public static Class<?> targetClass = CraftItemStack.class; //TODO get the class from effectLib
	
	public static Field craftItemStackHandleField;
	public static Method asNMSCopyMethod;
	public static Method asCraftCopyMethod;
	
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
			craftItemStackHandleField = targetClass.getDeclaredField("handle");
			craftItemStackHandleField.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		try {
			asNMSCopyMethod = ReflectionUtils.getMethod(targetClass, "asNMSCopy", net.minecraft.server.v1_8_R3.ItemStack.class);
			asNMSCopyMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			asCraftCopyMethod = targetClass.getDeclaredMethod("asCraftCopy", net.minecraft.server.v1_8_R3.ItemStack.class);
			asCraftCopyMethod.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}