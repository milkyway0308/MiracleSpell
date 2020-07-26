package com.nisovin.magicspells.castmodifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ProxyCondition extends Condition {
	
	private static Map<String, List<ProxyCondition>> uninitialized;
	
	private Condition actualCondition;
	
	private String conditionVar;
	
	//format this will accept is
	//<addonAPITag>:<externalConditionName>
	public ProxyCondition(String data) {
		if (uninitialized == null) uninitialized = new ConcurrentHashMap<>();
		if (!uninitialized.containsKey(data.toLowerCase())) uninitialized.put(data.toLowerCase(), new ArrayList<>());
		uninitialized.get(data.toLowerCase()).add(this);
	}
	
	public static void loadBackends(Map<String, Class<? extends Condition>> externalConditionClasses) {
		if (uninitialized != null && externalConditionClasses != null) {
			Set<String> keysToRemove = new HashSet<>();
			for (String key: uninitialized.keySet()) {
				if (externalConditionClasses.containsKey(key)) {
					Class<? extends Condition> clazz = externalConditionClasses.get(key);
					for (ProxyCondition c: uninitialized.get(key)) {
						try {
							c.actualCondition = clazz.newInstance();
							if (!c.actualCondition.setVar(c.conditionVar)) c.actualCondition = null;
						} catch (InstantiationException | IllegalAccessException e) {
						}
					}
					keysToRemove.add(key);
				}
			}
			for (String s: keysToRemove) {
				uninitialized.remove(s).clear();
			}
		}
	}
	
	@Override
	public boolean setVar(String var) {
		this.conditionVar = var;
		return true;
	}

	@Override
	public boolean check(Player player) {
		if (actualCondition == null) return false;
		return actualCondition.check(player);
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (actualCondition == null) return false;
		return actualCondition.check(player, target);
	}

	@Override
	public boolean check(Player player, Location location) {
		if (actualCondition == null) return false;
		return actualCondition.check(player, location);
	}

}
