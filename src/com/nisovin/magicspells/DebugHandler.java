package com.nisovin.magicspells;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class DebugHandler {
	
	public static void debugEffectInfo(String s) {
		if (MagicSpells.plugin.debug) {
			MagicSpells.plugin.getLogger().log(Level.INFO, s);
		}
	}
	
	public static void debugNull(Throwable t) {
		if (MagicSpells.plugin.debugNull) {
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugBadEnumValue(Class<? extends Enum<?>> e, String receivedValue) {
		try {
			Method getValuesMethod = e.getMethod("values");
			MagicSpells.plugin.getLogger().log(Level.WARNING, "Bad enum value of \"" + receivedValue + "\" received for type \"" + e.getName() + '\"');
			MagicSpells.plugin.getLogger().log(Level.WARNING, "Enum values are " + getValuesMethod.invoke(null));
		} catch (NoSuchMethodException |SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			MagicSpells.plugin.getLogger().severe("Bad news, one of the logging methods just failed hard");
			e1.printStackTrace();
		}
	}
	
	private static String throwableToString(Throwable t) {
		StringBuilder builder = new StringBuilder();
		for (StackTraceElement e: t.getStackTrace()) {
			builder.append(e.toString());
			builder.append('\n');
		}
		return builder.toString();
	}
	
	public static void debugNumberFormat(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) {
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugIllegalState(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugGeneral(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugNoClassDefFoundError(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugIOException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugFileNotFoundException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugIllegalStateException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugIllegalArgumentException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugIllegalAccessException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugInvocationTargetException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugNoSuchMethodException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugClassNotFoundException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugSecurityException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debugNoSuchFieldException(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void debug(Throwable t) {
		if (MagicSpells.plugin.debugNumberFormat) { //TODO setuo a different config node
			MagicSpells.plugin.getLogger().log(Level.WARNING, t.toString() + '\n' + throwableToString(t));
		}
	}
	
	public static void nullCheck(String s) {
		if (MagicSpells.plugin.debug) {
			MagicSpells.plugin.getLogger().warning(s);
		}
	}
	
	public static void nullCheck(Throwable t) {
		nullCheck(t.toString() + '\n' + throwableToString(t));
	}
	
	public static boolean isNullCheckEnabled() {
		return MagicSpells.plugin.debug;
	}
	
	public static boolean isSpellPreImpactEventCheckEnabled() {
		return MagicSpells.plugin.debug;
	}
	
}
