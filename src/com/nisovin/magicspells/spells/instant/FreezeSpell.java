package com.nisovin.magicspells.spells.instant;

import java.util.Random;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.SpellDamageSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.MagicConfig;

// TODO allow power to optionally control snowball count
// TODO allow power to optionally control slow amount
// TODO allow power to optionally control slow duration
// TODO should the effects on hit be fully configurable?
// TODO add a 'stop after time' option
// TODO add a 'hit at end' spell option for when it expires but is still in the air
// TODO add a spell on hit entity option
// TODO add a spell on hit ground option
public class FreezeSpell extends InstantSpell implements SpellDamageSpell {

	private int snowballs;
	private double horizSpread;
	private double vertSpread;
	private int damage;
	private String spellDamageType;
	private int slowAmount;
	private int slowDuration;
	private boolean snowballGravity;
	
	private float identifier;
	
	public FreezeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.snowballs = getConfigInt("snowballs", 15);
		this.horizSpread = getConfigInt("horizontal-spread", 15) / 10.0;
		this.vertSpread = getConfigInt("vertical-spread", 15) / 10.0;
		this.damage = getConfigInt("damage", 3);
		this.spellDamageType = getConfigString("spell-damage-type", "");
		this.slowAmount = getConfigInt("slow-amount", 3);
		this.slowDuration = getConfigInt("slow-duration", 40);
		this.snowballGravity = getConfigBoolean("gravity", true);
		
		this.identifier = (float)Math.random() * 20F;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Random rand = new Random();
			Vector mod;
			for (int i = 0; i < this.snowballs; i++) {
				Snowball snowball = player.launchProjectile(Snowball.class);
				MagicSpells.getVolatileCodeHandler().setGravity(snowball, this.snowballGravity);
				playSpellEffects(EffectPosition.PROJECTILE, snowball);
				playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, player.getLocation(), snowball.getLocation(), player, snowball);
				snowball.setFallDistance(this.identifier); // Tag the snowballs
				mod = new Vector((rand.nextDouble() - .5) * this.horizSpread, (rand.nextDouble() - .5) * this.vertSpread, (rand.nextDouble() - .5) * this.horizSpread);
				snowball.setVelocity(snowball.getVelocity().add(mod));
			}
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (this.damage <= 0) return;
		if (event.isCancelled()) return;
		if (!(event.getEntity() instanceof LivingEntity)) return;
		if (!(event.getDamager() instanceof Snowball)) return;
		if (event.getDamager().getFallDistance() != this.identifier) return;
		
		LivingEntity entity = (LivingEntity)event.getEntity();
		
		if (this.validTargetList.canTarget(entity)) {
			float power = 1;
			SpellTargetEvent e = new SpellTargetEvent(this, (Player)((Snowball)event.getDamager()).getShooter(), entity, power);
			EventUtil.call(e);
			if (e.isCancelled()) {
				event.setCancelled(true);
			} else {
				event.setDamage(this.damage * e.getPower());
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void applySlowEffect(EntityDamageByEntityEvent event) {
		if (this.slowAmount <= 0 || this.slowDuration <= 0) return;
		
		if (!(event.getDamager() instanceof Snowball)) return;
		if (event.getDamager().getFallDistance() != this.identifier) return;
		
		((LivingEntity)event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, this.slowDuration, this.slowAmount), true);
	}

	@Override
	public String getSpellDamageType() {
		return this.spellDamageType;
	}

}
