package com.nisovin.magicspells.util;

import org.bukkit.entity.Entity;

public class TargetInfo<E extends Entity> {

	private E target;
	private float power;
	
	public TargetInfo(E target, float power) {
		this.target = target;
		this.power = power;
	}
	
	public E getTarget() {
		return this.target;
	}
	
	public float getPower() {
		return this.power;
	}
	
}
