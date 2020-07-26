package com.nisovin.magicspells.castmodifiers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class TargetListener implements Listener {
	
	private List<IModifier> preModifierHooks;
	private List<IModifier> postModifierHooks;
	
	public TargetListener() {
		preModifierHooks = new CopyOnWriteArrayList<>();
		postModifierHooks = new CopyOnWriteArrayList<>();
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		for (IModifier premod: preModifierHooks) {
			if (!premod.apply(event)) return;
		}
		
		ModifierSet m = event.getSpell().getTargetModifiers();
		if (m != null) m.apply(event);
		
		for (IModifier postMod: postModifierHooks) {
			if (!postMod.apply(event)) return;
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSpellTarget(SpellTargetLocationEvent event) {
		for (IModifier premod: preModifierHooks) {
			if (!premod.apply(event)) return;
		}
		
		ModifierSet m = event.getSpell().getTargetModifiers();
		if (m != null) m.apply(event);
		
		for (IModifier postMod: postModifierHooks) {
			if (!postMod.apply(event)) return;
		}
	}
	
	public void addPreModifierHook(IModifier hook) {
		if (hook == null) return;
		preModifierHooks.add(hook);
	}
	
	public void addPostModifierHook(IModifier hook) {
		if (hook == null) return;
		postModifierHooks.add(hook);
	}
	
	public void unload() {
		preModifierHooks.clear();
		preModifierHooks = null;
		postModifierHooks.clear();
		postModifierHooks = null;
	}
	
}
