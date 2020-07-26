package com.nisovin.magicspells.util.expression;

public class SubtractionOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		return arg1.doubleValue() - arg2.doubleValue();
	}
	
}