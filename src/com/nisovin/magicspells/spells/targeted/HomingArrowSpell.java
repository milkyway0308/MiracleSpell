package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nisovin.magicspells.util.projectile.ProjectileManager;
import com.nisovin.magicspells.util.projectile.ProjectileManagers;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class HomingArrowSpell extends TargetedSpell implements TargetedEntitySpell, TargetedEntityFromLocationSpell {

	float velocity;
	int specialEffectInterval = 0;
	
	List<HomingArrow> arrows = new ArrayList<>();
	int monitor = 0;
	
	ProjectileManager projectileManager;
	
	public HomingArrowSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		velocity = getConfigFloat("velocity", 1F);
		specialEffectInterval = getConfigInt("special-effect-interval", 0);
		projectileManager = ProjectileManagers.getManager(getConfigString("projectile-type",  "arrow"));
	}

	private void fireHomingArrow(Player player, Location from, LivingEntity target, float power) {
		Projectile projectile;
		Vector v;
		if (from != null) {
			v = target.getLocation().toVector().subtract(from.toVector()).normalize();
			from = from.clone().setDirection(v);
			projectile = from.getWorld().spawn(from, projectileManager.getProjectileClass());
			if (player != null) projectile.setShooter(player);
		} else if (player != null) {
			projectile = projectileManager.launchProjectile(player);
			v = player.getLocation().getDirection();
		} else {
			return;
		}
		v.multiply(velocity * power);
		projectile.setVelocity(v);
		playSpellEffects(EffectPosition.PROJECTILE, projectile);
		playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, from, projectile.getLocation(), player, projectile);
		arrows.add(new HomingArrow(player, projectile, target, power));
		if (monitor == 0) monitor = MagicSpells.scheduleRepeatingTask(new HomingArrowMonitor(), 1, 1);

		if (from != null) {
			playSpellEffects(EffectPosition.CASTER, from);
		} else if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player);
		}
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
			if (targetInfo == null) return noTarget(player);
			fireHomingArrow(player, null, targetInfo.getTarget(), targetInfo.getPower());
			sendMessages(player, targetInfo.getTarget());
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) return false;
		fireHomingArrow(caster, null, target, power);			
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return false;
	}

	@Override
	public boolean castAtEntityFromLocation(Player caster, Location from, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) return false;
		fireHomingArrow(caster, from, target, power);
		return true;
	}

	@Override
	public boolean castAtEntityFromLocation(Location from, LivingEntity target, float power) {
		if (!validTargetList.canTarget(target)) return false;
		fireHomingArrow(null, from, target, power);
		return true;
	}
	
	class HomingArrowMonitor implements Runnable {
		
		int c = 0;
		
		@Override
		public void run() {
			c++;
			Iterator<HomingArrow> iter = arrows.iterator();
			while (iter.hasNext()) {
				HomingArrow arrow = iter.next();
				if (arrow.arrow.isDead() || arrow.arrow.isOnGround() || arrow.target.isDead() || !arrow.target.isValid()) {
					iter.remove();
					playSpellEffects(EffectPosition.TARGET, arrow.arrow.getLocation());
				} else { // if (arrow.arrow.getLocation().distanceSquared(arrow.target.getLocation()) > 5 * 5) {
					// TODO see what can be cleaned up here with constructing locations and vectors
					Vector v = arrow.target.getLocation().add(0, 0.75, 0).toVector().subtract(arrow.arrow.getLocation().toVector()).normalize();
					v.multiply(velocity * arrow.power);
					v.setY(v.getY() + 0.15);
					arrow.arrow.setVelocity(v);
					//Location l = arrow.arrow.getLocation().setDirection(v);
					//arrow.arrow.teleport(l);
					if (specialEffectInterval > 0 && c % specialEffectInterval == 0) playSpellEffects(EffectPosition.SPECIAL, arrow.arrow.getLocation());
				}
			}
			
			if (arrows.isEmpty()) {
				MagicSpells.cancelTask(monitor);
				monitor = 0;
			}
		}
	}
	
	class HomingArrow {
		
		Player shooter;
		Projectile arrow;
		LivingEntity target;
		float power;
		
		public HomingArrow(Player shooter, Projectile arrow, LivingEntity target, float power) {
			this.shooter = shooter;
			this.arrow = arrow;
			this.target = target;
			this.power = power;
		}
		
	}

}
