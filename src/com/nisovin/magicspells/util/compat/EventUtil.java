package com.nisovin.magicspells.util.compat;

import com.nisovin.magicspells.MagicSpells;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

// TODO add a utility to wrap operations in anticheat systems exemption
public class EventUtil {

	public static void call(Event event) {
		Bukkit.getPluginManager().callEvent(event);
	}
	
	public static void register(Listener listener, Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}
	
	public static void register(Listener listener) {
		register(listener, MagicSpells.plugin);
	}
	
}
