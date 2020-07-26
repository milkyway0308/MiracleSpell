package com.nisovin.magicspells.variables;

public class GlobalVariable extends Variable {

	double value = 0;
	
	@Override
	protected void init() {
		this.value = this.defaultValue;
	}
	
	@Override
	public boolean modify(String player, double amount) {
		double newvalue = this.value + amount;
		if (newvalue > this.maxValue) {
			newvalue = this.maxValue;
		} else if (newvalue < this.minValue) {
			newvalue = this.minValue;
		}
		if (this.value != newvalue) {
			this.value = newvalue;
			return true;
		}
		return false;
	}

	@Override
	public void set(String player, double amount) {
		this.value = amount;
	}

	@Override
	public double getValue(String player) {
		return this.value;
	}

	@Override
	public void reset(String player) {
		this.value = this.defaultValue;
	}
	
}
