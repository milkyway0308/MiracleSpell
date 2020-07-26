package com.nisovin.magicspells.util.cmd;

import com.nisovin.magicspells.exception.MagicException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArgAllable<E> extends Arg<Collection<E>> {
	
	private Arg<E> innerArg = null;
	public Arg<E> getInnerArg() { return this.innerArg; }
	
	private Set<String> alls = new HashSet<>();
	public Set<String> getAlls() { return new HashSet<>(this.alls); }
	public void setAlls(Collection<String> alls) {
		this.alls.clear();
		alls.forEach(string -> this.alls.add(string.toLowerCase()));
	}
	
	public ArgAllable(String name, Arg<E> arg) {
		super(name);
		this.innerArg = arg;
	}
	
	@Override
	protected Collection<E> readValueInner(String input) throws MagicException {
		if (this.alls.contains(input.toLowerCase())) return this.innerArg.getAll();
		return Collections.singleton(this.innerArg.readValue(input));
	}
	
	@Override
	public Collection<Collection<E>> getAll() {
		return null;
	}
	
}
