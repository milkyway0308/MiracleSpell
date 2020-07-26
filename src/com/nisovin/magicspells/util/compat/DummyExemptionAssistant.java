package com.nisovin.magicspells.util.compat;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

public class DummyExemptionAssistant implements ExemptionAssistant {
	
	@Override
	public <T> T exemptRunnable(Supplier<T> runnable, Player player, Collection<?> nodes) {
		return runnable.get();
	}
	
	@Override
	public Collection<Object> optimizeNodes(Object[] nodes) {
		return null;
	}
	
}
