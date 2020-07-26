package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IntMap<T> {

	private Map<T, Integer> map = new HashMap<>();
	
	public int put(T key, int value) {
		Integer prev = this.map.put(key, Integer.valueOf(value));
		if (prev != null) return prev.intValue();
		return 0;
	}
	
	public void set(T key, int value) {
		put(key, value);
	}
	
	public int get(T key) {
		Integer value = this.map.get(key);
		if (value != null) return value.intValue();
		return 0;
	}
	
	public int remove(T key) {
		Integer value = this.map.remove(key);
		if (value != null) return value.intValue();
		return 0;
	}
	
	public int size() {
		return this.map.size();
	}
	
	public boolean contains(T key) {
		return this.map.containsKey(key);
	}
	
	public boolean containsKey(T key) {
		return this.map.containsKey(key);
	}
	
	public boolean containsValue(int value) {
		return this.map.containsValue(Integer.valueOf(value));
	}
	
	public int increment(T key) {
		return increment(key, 1);
	}
	
	public int increment(T key, int amount) {
		int value = get(key) + amount;
		put(key, value);
		return value;
	}
	
	public int decrement(T key) {
		return decrement(key, 1);
	}
	
	public int decrement(T key, int amount) {
		int value = get(key) - amount;
		put(key, value);
		return value;
	}
	
	public int multiply(T key, int amount) {
		int value = get(key) * amount;
		put(key, value);
		return value;
	}
	
	public Set<T> keySet() {
		return this.map.keySet();
	}
	
	public void clear() {
		this.map.clear();
	}
	
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	public void putAll(IntMap<? extends T> otherMap) {
		this.map.putAll(otherMap.map);
	}
	
	public void putAll(Map<? extends T, Integer> otherMap) {
		this.map.putAll(otherMap);
	}
	
	public Map<T, Integer> getIntegerMap() {
		return this.map;
	}
	
	@Override
	public IntMap<T> clone() {
		IntMap<T> newMap = new IntMap<>();
		newMap.putAll(this.map);
		return newMap;
	}
	
	public void useTreeMap() {
		Map<T, Integer> newMap = new TreeMap<>();
		newMap.putAll(this.map);
		this.map.clear();
		this.map = newMap;
	}
	
}
