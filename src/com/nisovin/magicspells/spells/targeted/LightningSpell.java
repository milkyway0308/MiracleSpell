package com.nisovin.magicspells.spells.targeted;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class LightningSpell extends TargetedSpell implements TargetedLocationSpell {
	
	private boolean requireEntityTarget;
	private boolean checkPlugins;
	private double additionalDamage;
	private boolean noDamage;
	boolean chargeCreepers = true;
	boolean zapPigs = true;
	
	public LightningSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		requireEntityTarget = getConfigBoolean("require-entity-target", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
		additionalDamage = getConfigFloat("additional-damage", 0F);
		noDamage = getConfigBoolean("no-damage", false);
		chargeCreepers = getConfigBoolean("charge-creepers", true);
		zapPigs = getConfigBoolean("zap-pigs", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target;
			LivingEntity entityTarget = null;
			if (requireEntityTarget) {
				TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
				if (targetInfo != null) {
					entityTarget = targetInfo.getTarget();
					power = targetInfo.getPower();
				}
				if (entityTarget instanceof Player && checkPlugins) {
					MagicSpellsEntityDamageByEntityEvent event = new MagicSpellsEntityDamageByEntityEvent(player, entityTarget, DamageCause.ENTITY_ATTACK, 1 + additionalDamage);
					EventUtil.call(event);
					if (event.isCancelled()) entityTarget = null;
				}
				if (entityTarget != null) {
					target = entityTarget.getLocation().getBlock();
					if (additionalDamage > 0) entityTarget.damage(additionalDamage * power, player);
				} else {
					return noTarget(player);
				}
			} else {
				try {
					target = getTargetedBlock(player, power);
				} catch (IllegalStateException e) {
					DebugHandler.debugIllegalState(e);
					target = null;
				}
				if (target != null) {
					SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, player, target.getLocation(), power);
					EventUtil.call(event);
					if (event.isCancelled()) {
						target = null;
					} else {
						target = event.getTargetLocation().getBlock();
						power = event.getPower();
					}
				}
			}
			if (target != null) {
				lightning(target.getLocation());
				playSpellEffects(player, target.getLocation());
				if (entityTarget != null) {
					sendMessages(player, entityTarget);
					return PostCastAction.NO_MESSAGES;
				}
			} else {
				return noTarget(player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void lightning(Location target) {
		if (noDamage) {
			target.getWorld().strikeLightningEffect(target);
		} else {				
			LightningStrike strike = target.getWorld().strikeLightning(target);
			strike.setMetadata("MS" + internalName, new FixedMetadataValue(MagicSpells.plugin, new ChargeOption(chargeCreepers, zapPigs)));
		}
	}
	
	@EventHandler
	public void onCreeperCharge(CreeperPowerEvent event) {
		LightningStrike strike = event.getLightning();
		if (strike == null) return;
		List<MetadataValue> data = strike.getMetadata("MS" + internalName);
		if (data == null || data.isEmpty()) return;
		for (MetadataValue val: data) {
			ChargeOption option = (ChargeOption)val.value();
			if (!option.chargeCreeper) event.setCancelled(true);
			break;
		}
	}
	
	@EventHandler
	public void onPigZap(PigZapEvent event) {
		LightningStrike strike = event.getLightning();
		if (strike == null) return;
		List<MetadataValue> data = strike.getMetadata("MS" + internalName);
		if (data == null || data.isEmpty()) return;
		for (MetadataValue val: data) {
			ChargeOption option = (ChargeOption)val.value();
			if (!option.changePig) event.setCancelled(true);
		}
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		lightning(target);
		playSpellEffects(caster, target);
		return true;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		lightning(target);
		playSpellEffects(EffectPosition.CASTER, target);
		return true;
	}
	
	class ChargeOption {
		
		boolean chargeCreeper;
		boolean changePig;
		
		public ChargeOption(boolean creeper, boolean pigmen) {
			chargeCreeper = creeper;
			changePig = pigmen;
		}
		
	}
	
}
