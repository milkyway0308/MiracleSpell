package com.nisovin.magicspells.castmodifiers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.mana.ManaChangeReason;

public class ManaListener implements Listener {
	
	private List<IModifier> preModifierHooks;
	private List<IModifier> postModifierHooks;
	
	public ManaListener() {
		preModifierHooks = new CopyOnWriteArrayList<>();
		postModifierHooks = new CopyOnWriteArrayList<>();
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onManaRegen(ManaChangeEvent event) {
		if (event.getReason() == ManaChangeReason.REGEN) {
			ModifierSet modifiers = MagicSpells.getManaHandler().getModifiers();
			for (IModifier premod: preModifierHooks) {
				if (!premod.apply(event)) return;
			}
			if (modifiers != null) modifiers.apply(event);
			for (IModifier postMod: postModifierHooks) {
				if (!postMod.apply(event)) return;
			}
		}
	}
	
	public void unload() {
		preModifierHooks.clear();
		preModifierHooks = null;
		postModifierHooks.clear();
		postModifierHooks = null;
	}
	
}
