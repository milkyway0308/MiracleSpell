package com.nisovin.magicspells.spells.targeted;

import java.lang.Math;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class ParseSpell extends TargetedSpell {

	private String variableToParse;
	private String expectedValue;
	private String firstVariable;
	private String secondVariable;
	private String parseToVariable;
	private String parseTo;
	private String operation;
	private int op;

	public ParseSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		// Global Options
		this.operation = getConfigString("operation", "normal");
		this.parseToVariable = getConfigString("parse-to-variable", null);
		// Normal/Translate options
		this.variableToParse = getConfigString("variable-to-parse", null);
		this.expectedValue = getConfigString("expected-value", null);
		this.parseTo = getConfigString("parse-to", null);
		// Difference options
		this.firstVariable = getConfigString("first-variable", null);
		this.secondVariable = getConfigString("second-variable", null);
	}

	@Override
	public void initialize() {
		// Are we trying to translate variables?
		if(operation.contains("translate") || operation.contains("normal")) {
			op = 1;
			if (variableToParse == null) {
				MagicSpells.error("You must define a variable to parse for the '" + internalName + "' ParseSpell");
				return;
			}

			if (expectedValue == null) {
				MagicSpells.error("You must define an expected value to parse for the '" + internalName + "' ParseSpell");
				return;
			}

			if (parseToVariable == null) {
				MagicSpells.error("You must define a variable to parse to for the '" + internalName + "' ParseSpell");
				return;
			}
			if (MagicSpells.getVariableManager().getVariable(this.variableToParse) == null) {
				MagicSpells.error("invalid variable to parse to on '" + internalName + "'");
			}
		}
		// Are we determining difference?
		if(operation.contains("difference")) {
			op = 2;
			if (firstVariable == null || secondVariable == null) {
				MagicSpells.error("You must define a first and second variable for the '" + internalName + "' ParseSpell");
				return;
			}
			if (MagicSpells.getVariableManager().getVariable(this.firstVariable) == null) {
				MagicSpells.error("invalid first variable on '" + internalName + "'");
			}
			if (MagicSpells.getVariableManager().getVariable(this.secondVariable) == null) {
				MagicSpells.error("invalid second variable on '" + internalName + "'");
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
			if (targetInfo == null) return noTarget(player);
			Player target = targetInfo.getTarget();
			if (target == null) return noTarget(player);

			if (op == 1) {
				// Change the actual variable to the requested value.
				String receivedValue = MagicSpells.getVariableManager().getStringValue(variableToParse, target);

				// Do the values match?
				if (receivedValue.equals(expectedValue) || expectedValue.contains("any")) {
					MagicSpells.getVariableManager().set(parseToVariable, target, parseTo);

					playSpellEffects(player, target);
					}
				}
			if (op == 2) {
				// Grab the primary and secondary variables.
				double primary = MagicSpells.getVariableManager().getValue(firstVariable, target);
				double secondary = MagicSpells.getVariableManager().getValue(secondVariable, target);

				// Find the difference.
				double diff = Math.abs(primary - secondary);

				// Set the request variable to the final value
				MagicSpells.getVariableManager().set(parseToVariable, target, diff);

				playSpellEffects(player, target);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
}