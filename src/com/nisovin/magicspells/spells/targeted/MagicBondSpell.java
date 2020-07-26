package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellFilter;
import com.nisovin.magicspells.util.TargetInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author ChronoKeeper
 *
 */
public class MagicBondSpell extends TargetedSpell implements TargetedEntitySpell {
	
    private int duration;
    private String strDurationEnd;
    private Map<Player, Player> bondTarget = new HashMap<>();
    private SpellFilter filter;

    public MagicBondSpell(MagicConfig config, String spellName){
        super(config, spellName);
        duration = getConfigInt("duration", 200);
        strDurationEnd = getConfigString("str-duration", null);
        List<String> spells = getConfigStringList("spells", null);
        List<String> deniedSpells = getConfigStringList("denied-spells", null);
        List<String> tagList = getConfigStringList("spell-tags", null);
        List<String> deniedTagList = getConfigStringList("denied-spell-tags", null);
        filter = new SpellFilter(spells, deniedSpells, tagList, deniedTagList);
    }

    @Override
    public PostCastAction castSpell(final Player player, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<Player> target = getTargetedPlayer(player, power);
            if (target == null) return noTarget(player);

            final Player targetedPlayer = target.getTarget();
            playSpellEffects(player, targetedPlayer);
            bondTarget.put(player, targetedPlayer);
            final SpellMonitor monitorBond = new SpellMonitor(player, targetedPlayer, power);
            MagicSpells.registerEvents(monitorBond);
            
            MagicSpells.scheduleDelayedTask(new Runnable() {
                @Override
                public void run() {
                    if (strDurationEnd != null && !strDurationEnd.isEmpty()) {
                        MagicSpells.sendMessage(player, strDurationEnd);
                        MagicSpells.sendMessage(targetedPlayer, strDurationEnd);
                    }
                    bondTarget.remove(player);
                    
                    HandlerList.unregisterAll(monitorBond);
                }
            }, duration);

        }
        return PostCastAction.HANDLE_NORMALLY;
    }


    @Override
    public boolean castAtEntity(Player caster, LivingEntity target, float power) {
        playSpellEffects(caster, target);
        return true;
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        playSpellEffects(EffectPosition.TARGET, target);
        return true;
    }
    
    class SpellMonitor implements Listener {
    	
        Player caster;
        Player target;
        float power;
        
        public SpellMonitor(Player caster, Player target, float power) {
            this.caster = caster;
            this.target = bondTarget.get(caster);
            this.power = power;
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e){
            if (bondTarget.containsKey(e.getPlayer()) || bondTarget.containsValue(e.getPlayer())){
                bondTarget.remove(caster);
            }
        }

        @EventHandler
        public void onPlayerSpellCast(SpellCastEvent e) {
            if (e.getCaster() != caster || e.getSpell() instanceof MagicBondSpell) return;
            if (!e.getSpell().onCooldown(caster) && bondTarget.containsKey(caster) && bondTarget.containsValue(target) && !target.isDead() && filter.check(e.getSpell())) {
                e.getSpell().cast(target);
            }
        }
        
        @Override
        public boolean equals(Object other) {
        	if (other == null) return false;
        	if (!getClass().getName().equals(other.getClass().getName())) return false;
        	SpellMonitor otherMonitor = (SpellMonitor)other;
        	if (otherMonitor.caster != caster) return false;
        	if (otherMonitor.target != target) return false;
        	if (otherMonitor.power != power) return false;
        	return true;
        }

    }
    
}
