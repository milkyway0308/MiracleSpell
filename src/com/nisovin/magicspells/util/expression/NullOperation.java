package com.nisovin.magicspells.util.expression;

public class NullOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		//just return the first value
		return arg1;
	}
	
	@Override
	public boolean singleInput() {
		return true;
	}
	
}