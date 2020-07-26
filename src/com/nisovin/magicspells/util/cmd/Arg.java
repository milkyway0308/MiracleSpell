package com.nisovin.magicspells.util.cmd;

import com.nisovin.magicspells.exception.MagicException;

public abstract class Arg<T> implements Allable<T> {

	private String name;
	public String getName() { return this.name; }
	
	private T defaultValue = null;
	public T getDefaultValue() { return this.defaultValue; }
	public void setDefaultValue(T defaultValue) { this.defaultValue = defaultValue; }
	
	private boolean innerAllowsEmpty = false;
	protected void setInnerAllowsEmpty(boolean allowsEmpty) { this.innerAllowsEmpty = allowsEmpty; }
	public boolean innerAllowsEmpty() { return this.innerAllowsEmpty; }
	
	private boolean shouldTrim = true;
	protected void setShouldTrim(boolean shouldTrim) { this.shouldTrim = shouldTrim; }
	public boolean shouldTrim() { return this.shouldTrim; }
	
	public Arg(String name) {
		this.name = name;
	}
	
	public Arg(String name, T defaultValue) {
		this(name);
		this.setDefaultValue(defaultValue);
	}
	
	public T readValue(String input) throws MagicException {
		if (!this.innerAllowsEmpty()) {
			if (input == null) return this.getDefaultValue();
			if (input.trim().isEmpty()) return this.getDefaultValue();
		}
		if (this.shouldTrim() && input != null) input = input.trim();
		return readValueInner(input);
	}
	
	protected abstract T readValueInner(String input) throws MagicException;
	
}
