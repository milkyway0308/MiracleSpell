package com.nisovin.magicspells.spells;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;

/**
 * ArrowSpell<br>
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
 *         <th>
 *             Default
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>bow-name</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             <code>null</code>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>spell-on-hit-entity</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             <code>null</code>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>spell-on-hit-ground</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             <code>null</code>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>use-bow-force</code>
 *         </td>
 *         <td>
 *             Boolean
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *         <td>
 *             <code>true</code>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *         <td>
 *         </td>
 *     </tr>
 * </table>
 */
public class ArrowSpell extends Spell {

	private static ArrowSpellHandler handler;
	
	String bowName;
	
	String spellNameOnHitEntity;
	
	String spellNameOnHitGround;
	
	Subspell spellOnHitEntity;
	Subspell spellOnHitGround;
	
	boolean useBowForce;
	
	private static final String METADATA_KEY = "MSArrowSpell";
	
	public ArrowSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.bowName = ChatColor.translateAlternateColorCodes('&', getConfigString("bow-name", null));
		this.spellNameOnHitEntity = getConfigString("spell-on-hit-entity", null);
		this.spellNameOnHitGround = getConfigString("spell-on-hit-ground", null);
		this.useBowForce = getConfigBoolean("use-bow-force", true);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (this.spellNameOnHitEntity != null && !this.spellNameOnHitEntity.isEmpty()) {
			Subspell spell = new Subspell(this.spellNameOnHitEntity);
			if (spell.process() && spell.isTargetedEntitySpell()) this.spellOnHitEntity = spell;
		}
		if (this.spellNameOnHitGround != null && !this.spellNameOnHitGround.isEmpty()) {
			Subspell spell = new Subspell(this.spellNameOnHitGround);
			if (spell.process() && spell.isTargetedLocationSpell()) this.spellOnHitGround = spell;
		}
		
		if (handler == null) handler = new ArrowSpellHandler();
		handler.registerSpell(this);
	}
	
	@Override
	public void turnOff() {
		super.turnOff();
		if (handler == null) return;
		handler.turnOff();
		handler = null;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		return PostCastAction.ALREADY_HANDLED;
	}

	@Override
	public boolean canCastWithItem() {
		return false;
	}

	@Override
	public boolean canCastByCommand() {
		return false;
	}
	
	class ArrowSpellHandler implements Listener {
		
		Map<String, ArrowSpell> spells = new HashMap<>();
		
		public ArrowSpellHandler() {
			registerEvents(this);
		}
		
		public void registerSpell(ArrowSpell spell) {
			this.spells.put(spell.bowName, spell);
		}
		
		@EventHandler
		public void onArrowLaunch(EntityShootBowEvent event) {
			if (event.getEntity().getType() != EntityType.PLAYER) return;
			Player shooter = (Player)event.getEntity();
			ItemStack inHand = HandHandler.getItemInMainHand(shooter);
			if (inHand == null || inHand.getType() != Material.BOW) return;
			String bowName = inHand.getItemMeta().getDisplayName();
			if (bowName == null) return;
			if (bowName.isEmpty()) return;
			Spellbook spellbook = MagicSpells.getSpellbook(shooter);
			ArrowSpell spell = this.spells.get(bowName);
			if (spell == null) return;
			if (!spellbook.hasSpell(spell)) return;
			if (!spellbook.canCast(spell)) return;
			SpellReagents reagents = spell.reagents.clone();
			SpellCastEvent castEvent = new SpellCastEvent(spell, shooter, SpellCastState.NORMAL, useBowForce ? event.getForce() : 1.0F, null, cooldown, reagents, castTime);
			EventUtil.call(castEvent);
			Entity projectile = event.getProjectile();
			if (!castEvent.isCancelled()) {
				projectile.setMetadata(METADATA_KEY, new FixedMetadataValue(MagicSpells.plugin, new ArrowSpellData(spell, castEvent.getPower(), castEvent.getReagents())));
				spell.playSpellEffects(EffectPosition.PROJECTILE, event.getProjectile());
				spell.playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, shooter.getLocation(), projectile.getLocation(), shooter, projectile);
			} else {
				event.setCancelled(true);
				projectile.remove();
			}
		}

		@EventHandler
		public void onArrowHit(ProjectileHitEvent event) {
			final Projectile arrow = event.getEntity();
			if (arrow.getType() != EntityType.ARROW) return;
			List<MetadataValue> metas = arrow.getMetadata(METADATA_KEY);
			if (metas == null || metas.isEmpty()) return;
			for (MetadataValue meta : metas) {
				final ArrowSpellData data = (ArrowSpellData)meta.value();
				if (data.spell.spellOnHitGround != null) {
					MagicSpells.scheduleDelayedTask(new Runnable() {
						@Override
						public void run() {
							Player shooter = (Player) arrow.getShooter();
							if (data.casted) return;
							if (data.spell.onCooldown(shooter)) {
								MagicSpells.sendMessage(formatMessage(strOnCooldown, "%c", Math.round(getCooldown(shooter)) + ""), shooter, null);
								return;
							}
							if (!data.spell.hasReagents(shooter, data.arrowSpellDataReagents)) {
								MagicSpells.sendMessage(strMissingReagents, shooter, null);
								return;
							}

							boolean success = data.spell.spellOnHitGround.castAtLocation(shooter, arrow.getLocation(), data.power);
							if (success) {
								data.spell.setCooldown(shooter, data.spell.cooldown);
								data.spell.removeReagents(shooter, data.arrowSpellDataReagents);
							}
							data.casted = true;
							arrow.removeMetadata(METADATA_KEY, MagicSpells.plugin);
						}
					}, 0);
				}
				break;
			}
			arrow.remove();
		}

		@EventHandler(ignoreCancelled=true)
		public void onArrowHitEntity(EntityDamageByEntityEvent event) {
			if (event.getDamager().getType() != EntityType.ARROW) return;
			if (!(event.getEntity() instanceof LivingEntity)) return;
			Projectile arrow = (Projectile)event.getDamager();
			List<MetadataValue> metas = arrow.getMetadata(METADATA_KEY);
			if (metas == null || metas.isEmpty()) return;
			Player shooter = (Player)arrow.getShooter();
			for (MetadataValue meta : metas) {
				ArrowSpellData data = (ArrowSpellData)meta.value();
				if (!data.spell.onCooldown(shooter)) {
					if (data.spell.spellOnHitEntity != null) {
						SpellTargetEvent evt = new SpellTargetEvent(data.spell, shooter, (LivingEntity)event.getEntity(), data.power);
						EventUtil.call(evt);
						if (!evt.isCancelled()) {
							data.spell.spellOnHitEntity.castAtEntity(shooter, (LivingEntity)event.getEntity(), evt.getPower());
							data.spell.setCooldown(shooter, data.spell.cooldown);
						}
						data.casted = true;
					}
				}
				break;
			}
			arrow.remove();
			arrow.removeMetadata(METADATA_KEY, MagicSpells.plugin);
		}
		
		public void turnOff() {
			unregisterEvents(this);
			this.spells.clear();
		}
		
	}
	
	class ArrowSpellData {
		
		ArrowSpell spell;
		boolean casted = false;
		float power = 1.0F;
		SpellReagents arrowSpellDataReagents;
		
		public ArrowSpellData(ArrowSpell spell, float power, SpellReagents reagents) {
			this.spell = spell;
			this.power = power;
			this.arrowSpellDataReagents = reagents;
		}
		
	}

}
