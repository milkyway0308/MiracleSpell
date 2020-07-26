package com.nisovin.magicspells.spells.buff;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellFilter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;

public class ImpactRecordSpell extends BuffSpell {
	
	// The names of the players currently buffed
	private HashSet<String> recorders;
	
	// Should it also record hits that were cancelled?
	private boolean recordCancelled = false;
	
	// The name of the variable to save the hits to
	private String variableName;
	
	// Filter the spells that can be recorded
	private SpellFilter recordFilter;
	
	public ImpactRecordSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		recordCancelled = getConfigBoolean("record-cancelled", false);
		variableName = getConfigString("variable-name", null);
		recordFilter = SpellFilter.fromConfig(config, "spells." + this.internalName + ".filter");
		
		recorders = new HashSet<>();
	}
	
	@Override
	public void initialize() {
		// Super
		super.initialize();
		
		// Variable name non null?
		if (this.variableName == null) {
			MagicSpells.error("no variable defined on '" + internalName + "'");
			return;
		}
		
		// Make sure the variable exists
		if (MagicSpells.getVariableManager().getVariable(this.variableName) == null) {
			MagicSpells.error("invalid variable-name on '" + internalName + "'");
			this.variableName = null;
		}
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		recorders.add(player.getName());
		return true;
	}

	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpellTargeted(SpellTargetEvent event) {
		// Is cancel state acceptable
		if (event.isCancelled() && !this.recordCancelled) return;
		
		// Is it a player?
		LivingEntity target = event.getTarget();
		if (!(target instanceof Player)) return;
		
		// Do they have the buff?
		Player playerTarget = (Player) target;
		if (!recorders.contains(target.getName())) return;
		
		// Check the spell filter
		Spell spell = event.getSpell();
		if (!recordFilter.check(spell)) return;
		
		// Charge cost
		addUseAndChargeCost(playerTarget);
		
		// Save to the variable
		MagicSpells.getVariableManager().set(this.variableName, playerTarget, spell.getInternalName());
	}

	@Override
	public void turnOffBuff(Player player) {
		recorders.remove(player.getName());
	}
	
	@Override
	protected void turnOff() {
		recorders.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return recorders.contains(player.getName());
	}

}
