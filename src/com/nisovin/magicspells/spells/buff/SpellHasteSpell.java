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

/**
 * SpellHasteSpell<br>
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
 *             cast-time-mod-amt
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             cooldown-mod-amt
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class SpellHasteSpell extends BuffSpell {
    
    private float castTimeModAmt;
    private float cooldownModAmt;
    
    private HashMap<String, Float> spellTimersModified;
    
    private SpellFilter filter;
    
    public SpellHasteSpell(MagicConfig config, String spellname) {
        super(config, spellname);
	
		this.castTimeModAmt = getConfigInt("cast-time-mod-amt", -25) / 100F;
		this.cooldownModAmt = getConfigInt("cooldown-mod-amt", -25) / 100F;
	
		this.spellTimersModified = new HashMap<>();
        
        List<String> spells = getConfigStringList("spells", null);
		List<String> deniedSpells = getConfigStringList("denied-spells", null);
		List<String> tagList = getConfigStringList("spell-tags", null);
		List<String> deniedTagList = getConfigStringList("denied-spell-tags", null);
	
		this.filter = new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
    }
    
    @Override
    public boolean castBuff(Player player, float power, String[] args) {
		this.spellTimersModified.put(player.getName(), power);
        return true;
    }
    
    @EventHandler (priority=EventPriority.MONITOR)
    public void onSpellSpeedCast(SpellCastEvent event) {
    	if (!this.filter.check(event.getSpell())) return;
    	
    	Float power = this.spellTimersModified.get(event.getCaster().getName());
    	if (power != null) {
    		// Modify cast time
    		if (this.castTimeModAmt != 0) {
	            int ct = event.getCastTime();
	            float newCT = ct + (this.castTimeModAmt * power * ct);
	            if (newCT < 0) newCT = 0;
	            event.setCastTime(Math.round(newCT));
    		}
    		// Modify cooldown
    		if (this.cooldownModAmt != 0) {
	            float cd = event.getCooldown();
	            float newCD = cd + (this.cooldownModAmt * power * cd);
	            if (newCD < 0) newCD = 0;
	            event.setCooldown(newCD);
    		}
    	}
    }
    
    @Override
    public boolean isActive(Player player) {
        return this.spellTimersModified.containsKey(player.getName());
    }

    @Override
    public void turnOffBuff(Player player) {
		this.spellTimersModified.remove(player.getName());
    }

    @Override
    protected void turnOff() {
		this.spellTimersModified.clear();
    }
    
}
