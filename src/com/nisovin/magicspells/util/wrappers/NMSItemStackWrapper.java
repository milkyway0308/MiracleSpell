package com.nisovin.magicspells.util.wrappers;

import java.lang.reflect.Method;

import de.slikey.effectlib.util.ReflectionUtils;

public class NMSItemStackWrapper {

	public static Class targetClass = net.minecraft.server.v1_8_R3.ItemStack.class; //TODO get this class dynamically
	public static Method getTagMethod; //TODO initialize this
	public static Method setTagMethod;
	
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
			getTagMethod = ReflectionUtils.getMethod(targetClass, "getTag");
			getTagMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		try {
			setTagMethod = ReflectionUtils.getMethod(targetClass, "setTag", NBTTagCompoundWrapper.targetClass);
			setTagMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
