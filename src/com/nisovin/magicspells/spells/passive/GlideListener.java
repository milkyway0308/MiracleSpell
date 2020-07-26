package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import java.util.ArrayList;
import java.util.List;

// No trigger variable is currently used
public class GlideListener extends PassiveListener {

	List<PassiveSpell> glide = null;
	List<PassiveSpell> stopGlide = null;
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (PassiveTrigger.START_GLIDE.contains(trigger)) {
			if (glide == null) glide = new ArrayList<>();
			glide.add(spell);
		} else if (PassiveTrigger.STOP_GLIDE.contains(trigger)) {
			if (stopGlide == null) stopGlide = new ArrayList<>();
			stopGlide.add(spell);
		}
	}
	
	@OverridePriority
	@EventHandler
	public void onGlide(EntityToggleGlideEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		if (event.isGliding()) {
			if (glide != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				for (PassiveSpell spell : glide) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(player);
					if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
					event.setCancelled(true);
				}
			}
		} else {
			if (stopGlide != null) {
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				for (PassiveSpell spell : stopGlide) {
					if (!isCancelStateOk(spell, event.isCancelled())) continue;
					if (!spellbook.hasSpell(spell, false)) continue;
					boolean casted = spell.activate(player);
					if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
					event.setCancelled(true);
				}
			}
		}
	}

}
