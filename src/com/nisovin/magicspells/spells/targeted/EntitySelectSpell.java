package com.nisovin.magicspells.spells.targeted;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class EntitySelectSpell extends TargetedSpell {
	
	// Weak reference so we don't hold on to things we shouldn't be
	private Map<String, WeakReference<LivingEntity>> targets;
	
	public EntitySelectSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		targets = new HashMap<>();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state,
			float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
			if (targetInfo == null || targetInfo.getTarget() == null) return noTarget(player);
			
			targets.put(player.getName(), new WeakReference<>(targetInfo.getTarget()));
			
			sendMessages(player, targetInfo.getTarget());
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public LivingEntity getTarget(Player player) {
		String playerName = player.getName();
		if (!targets.containsKey(playerName)) return null;
		
		WeakReference<LivingEntity> ref = targets.get(playerName);
		
		if (ref == null) {
			targets.remove(playerName);
			return null;
		}
		
		return ref.get();
	}
	
	@Override
	public void turnOff() {
		super.turnOff();
		targets.clear();
		targets = null;
	}
	
	private void remove(Player player) {
		targets.remove(player.getName());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// This is needed so we don't have memory leaks
		remove(event.getPlayer());
	}
	
}
