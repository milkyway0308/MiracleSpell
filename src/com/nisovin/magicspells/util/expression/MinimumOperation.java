package com.nisovin.magicspells.util.expression;

public class MinimumOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		return Math.min(arg1.doubleValue(), arg2.doubleValue());
	}

}
