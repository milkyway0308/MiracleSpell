package com.nisovin.magicspells.util.expression;

public class AbsoluteValueOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		//just ignore the second value, only caring about the first one
		return Math.abs(arg1.doubleValue());
	}
	
	@Override
	public boolean singleInput() {
		return true;
	}

}
