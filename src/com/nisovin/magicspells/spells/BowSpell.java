package com.nisovin.magicspells.spells;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;

public class BowSpell extends Spell {

	private static BowSpellHandler handler;
	
	BowSpell thisSpell;
	
	@ConfigData(field="bow-name", dataType="String", defaultValue="null")
	String bowName;
	
	@ConfigData(field="spell", dataType="String", defaultValue="null")
	String spellNameOnShoot;
	
	Subspell spellOnShoot;
	
	@ConfigData(field="use-bow-force", dataType="boolean", defaultValue="true")
	boolean useBowForce;
	
	public BowSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.thisSpell = this;
		this.bowName = ChatColor.translateAlternateColorCodes('&', getConfigString("bow-name", null));
		this.spellNameOnShoot = getConfigString("spell", null);
		this.useBowForce = getConfigBoolean("use-bow-force", true);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (this.spellNameOnShoot != null && !this.spellNameOnShoot.isEmpty()) {
			this.spellOnShoot = new Subspell(this.spellNameOnShoot);
			if (!this.spellOnShoot.process()) {
				this.spellOnShoot = null;
				MagicSpells.error("Bow spell '" + this.internalName + "' has invalid spell defined: '" + this.spellNameOnShoot + '\'');
			}
		}
		
		if (handler == null) handler = new BowSpellHandler();
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
	
	class BowSpellHandler implements Listener {
		
		Map<String, BowSpell> spells = new HashMap<>();
		
		public BowSpellHandler() {
			registerEvents(this);
		}
		
		public void registerSpell(BowSpell spell) {
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
			BowSpell spell = this.spells.get(bowName);
			
			if (spell == null) return;
			if (!spellbook.hasSpell(spell)) return;
			if (!spellbook.canCast(spell)) return;
			if (thisSpell.onCooldown(shooter)) {
				MagicSpells.sendMessage(formatMessage(thisSpell.strOnCooldown, "%c", Math.round(getCooldown(shooter)) + ""), shooter, null);
				return;
			}
			if (!thisSpell.hasReagents(shooter)) {
				MagicSpells.sendMessage(thisSpell.strMissingReagents, shooter, null);
				return;
			}

			SpellCastEvent evt1 = new SpellCastEvent(thisSpell, shooter, SpellCastState.NORMAL, useBowForce ? event.getForce() : 1.0F, null, thisSpell.cooldown, thisSpell.reagents, 0);
			EventUtil.call(evt1);
			if (evt1.isCancelled()) return;
			
			event.setCancelled(true);
			event.getProjectile().remove();
			spell.spellOnShoot.cast(shooter, evt1.getPower());
			thisSpell.setCooldown(shooter, thisSpell.cooldown);
			thisSpell.removeReagents(shooter);
			SpellCastedEvent evt2 = new SpellCastedEvent(thisSpell, shooter, SpellCastState.NORMAL, evt1.getPower(), null, thisSpell.cooldown, thisSpell.reagents, PostCastAction.HANDLE_NORMALLY);
			EventUtil.call(evt2);
		}
		
		public void turnOff() {
			unregisterEvents(this);
			spells.clear();
		}
		
	}
	
}
