package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellFilter;
import com.nisovin.magicspells.util.SpellReagents;

public class ClaritySpell extends BuffSpell {

	float multiplier;
	
	private SpellFilter filter;
	
	Map<String, Float> buffed = new HashMap<>();
	
	public ClaritySpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		multiplier = getConfigFloat("multiplier", 0.5F);
		List<String> spells = getConfigStringList("spells", null);
		List<String> deniedSpells = getConfigStringList("denied-spells", null);
		List<String> tagList = getConfigStringList("spell-tags", null);
		List<String> deniedTagList = getConfigStringList("denied-spell-tags", null);
		filter = new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		buffed.put(player.getName(), power);
		return true;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSpellCast(SpellCastEvent event) {
		Player caster = event.getCaster();
		String casterName = caster.getName();
		if (!buffed.containsKey(casterName)) return;
		if (!filter.check(event.getSpell())) return;
		
		float power = buffed.get(casterName);
		float mod = multiplier;
		
		if (multiplier < 1) {
			mod *= 1/power;
		} else if (multiplier > 1) {
			mod *= power;
		}
		
		SpellReagents reagents = event.getReagents();
		if (reagents != null) event.setReagents(reagents.multiply(mod));
		
		addUseAndChargeCost(caster);
	}
	
	@Override
	public boolean isActive(Player player) {
		return buffed.containsKey(player.getName());
	}

	@Override
	protected void turnOffBuff(Player player) {
		buffed.remove(player.getName());
	}

	@Override
	protected void turnOff() {
		buffed.clear();
	}

}
