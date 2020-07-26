package com.nisovin.magicspells.util.compat;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.compat.nocheatplus.NoCheatPlusExemptionAid;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;
import java.util.function.Supplier;

public class CompatBasics {
	
	public static boolean pluginEnabled(String plugin) {
		return Bukkit.getPluginManager().isPluginEnabled(plugin);
	}
	
	public static Plugin getPlugin(String name) {
		return Bukkit.getPluginManager().getPlugin(name);
	}
	
	public static <T> RegisteredServiceProvider<T> getServiceProvider(Class<T> clazz) {
		return Bukkit.getServer().getServicesManager().getRegistration(clazz);
	}
	
	public static boolean runsWithoutError(Runnable runnable) {
		try {
			runnable.run();
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}
	
	public static boolean doesClassExist(String name) {
		try {
			return Class.forName(name) != null;
		} catch (Throwable throwable) {
			return false;
		}
	}
	
	public static ExemptionAssistant activeExemptionAssistant = null;
	
	public static <T> T exemptAction(Supplier<T> runnable, Player player, Collection<?> checks) {
		if (activeExemptionAssistant != null && player != null) {
			return activeExemptionAssistant.exemptRunnable(runnable, player, checks);
		} else {
			return runnable.get();
		}
	}
	
	public static void setupExemptionAssistant() {
		// Already set up
		if (activeExemptionAssistant != null) return;
		
		if (!MagicSpells.plugin.allowAnticheatIntegrations) {
			activeExemptionAssistant = new DummyExemptionAssistant();
			return;
		}
		
		if (pluginEnabled("NoCheatPlus")) {
			activeExemptionAssistant = new NoCheatPlusExemptionAid();
			return;
		}
		
		// Nothing else has been successfully enabled, so just use this
		activeExemptionAssistant = new DummyExemptionAssistant();
	}
	
	public static void destructExemptionAssistant() {
		// Nothing there already
		if (activeExemptionAssistant == null) return;
		
		// Put any additional cleanup calls here
		activeExemptionAssistant = null;
	}
	
}
