package com.nisovin.magicspells.util.expression;

public class MaximumOperation extends Operation {

	@Override
	public Number evaluate(Number arg1, Number arg2) {
		return Math.max(arg1.doubleValue(), arg2.doubleValue());
	}

}
