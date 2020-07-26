package com.nisovin.magicspells.spells.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spells.SpellDamageSpell;
import com.nisovin.magicspells.util.MagicConfig;

/**
 * ResistSpell<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             spell-damage-types
 *         </td>
 *         <td>
 *             String List
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             normal-damage-types
 *         </td>
 *         <td>
 *             String List
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             multiplier
 *         </td>
 *         <td>
 *             Float
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class ResistSpell extends BuffSpell {

	List<String> spellDamageTypes;
	List<DamageCause> normalDamageTypes;
	float multiplier;	
	
	Map<String, Float> buffed = new HashMap<>();
	
	public ResistSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		spellDamageTypes = getConfigStringList("spell-damage-types", null);
		List<String> list = getConfigStringList("normal-damage-types", null);
		multiplier = getConfigFloat("multiplier", 0.5F);
		
		if (list != null) {
			normalDamageTypes = new ArrayList<>();
			for (String s : list) {
				for (DamageCause cause : DamageCause.values()) {
					if (!cause.name().equalsIgnoreCase(s)) continue;
					normalDamageTypes.add(cause);
					break;
				}
			}
			if (normalDamageTypes.isEmpty()) normalDamageTypes = null;
		}
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		buffed.put(player.getName(), power);
		return true;
	}
	
	@EventHandler
	public void onSpellDamage(SpellApplyDamageEvent event) {
		if (spellDamageTypes == null) return;
		if (!(event.getSpell() instanceof SpellDamageSpell)) return;
		if (!(event.getTarget() instanceof Player)) return;
		if (!isActive((Player)event.getTarget())) return;
		SpellDamageSpell spell = (SpellDamageSpell)event.getSpell();
		String spellDamageType = spell.getSpellDamageType();
		if (spellDamageType == null) return;
		if (!spellDamageTypes.contains(spellDamageType)) return;
		Player player = (Player)event.getTarget();
		float power = multiplier;
		if (multiplier < 1) {
			power *= 1 / buffed.get(player.getName());
		} else if (multiplier > 1) {
			power *= buffed.get(player.getName());
		}
		event.applyDamageModifier(power);
		addUseAndChargeCost(player);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (normalDamageTypes == null) return;
		if (!normalDamageTypes.contains(event.getCause())) return;
		Entity entity = event.getEntity();
		if (entity instanceof Player && isActive((Player)entity)) {
			Player player = (Player)entity;
			String playerName = player.getName();
			float mult = multiplier;
			if (multiplier < 1) {
				mult *= 1 / buffed.get(playerName);
			} else if (multiplier > 1) {
				mult *= buffed.get(playerName);
			}
			event.setDamage(event.getDamage() * mult);
			addUseAndChargeCost(player);
		}
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
