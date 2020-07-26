package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public interface IModifier {
	
	boolean apply(SpellCastEvent event);
	boolean apply(ManaChangeEvent event);
	boolean apply(SpellTargetEvent event);
	boolean apply(SpellTargetLocationEvent event);
	boolean apply(MagicSpellsGenericPlayerEvent event);
	boolean check(Player player);

}
