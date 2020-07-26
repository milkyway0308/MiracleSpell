package com.nisovin.magicspells.util.expression;

public class PowerOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		return Math.pow(arg1.doubleValue(), arg2.doubleValue());
	}
}