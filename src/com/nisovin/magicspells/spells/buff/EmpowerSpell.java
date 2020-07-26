package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellFilter;

public class EmpowerSpell extends BuffSpell {

	private float extraPower;
	private float maxPower;
	
	private SpellFilter filter;
	
	private HashMap<String, Float> empowered;
	
	public EmpowerSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		extraPower = getConfigFloat("power-multiplier", 1.5F);
		maxPower = getConfigFloat("max-power-multiplier", 1.5F);
		
		List<String> spells = getConfigStringList("spells", null);
		List<String> deniedSpells = getConfigStringList("denied-spells", null);
		List<String> tagList = getConfigStringList("spell-tags", null);
		List<String> deniedTagList = getConfigStringList("denied-spell-tags", null);
		
		filter = new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
		empowered = new HashMap<>();
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		float p = power * extraPower;
		if (p > maxPower) p = maxPower;
		empowered.put(player.getName(), p);
		return true;
	}
	
	@Override
	public boolean recastBuff(Player player, float power, String[] args) {
		// TODO flatten this
		if (maxPower > extraPower) {
			String playerName = player.getName();
			float p = empowered.get(playerName);
			p += power * extraPower;
			if (p > maxPower) p = maxPower;
			empowered.put(playerName, p);
		}
		return true;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onSpellCast(SpellCastEvent event) {
		Player player = event.getCaster();
		if (player == null) return;
		
		String playerName = player.getName();
		if (!empowered.containsKey(playerName)) return;
		if (!filter.check(event.getSpell())) return;
		
		event.increasePower(empowered.get(playerName));
		addUseAndChargeCost(player);
	}
	
	@Override
	public void turnOffBuff(Player player) {
		empowered.remove(player.getName());
	}

	@Override
	protected void turnOff() {
		empowered.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return empowered.containsKey(player.getName());
	}

}
