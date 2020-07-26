package com.nisovin.magicspells.variables;

public abstract class MetaVariable extends Variable {

	public MetaVariable() {
		this.permanent = false;
		this.bossBar = null;
		this.expBar = false;
		this.objective = null;
		this.minValue = Double.MIN_VALUE;
	}
	
	@Override
	protected void init() {
		this.permanent = false;
	}
	
	@Override
	public boolean modify(String player, double amount) {
		// No op
		return false;
	}

	@Override
	public void set(String player, double amount) {
		// No op
	}
	
	@Override
	public void reset(String player) {
		// No op
	}

}
