package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell.PostCastAction;
import com.nisovin.magicspells.Spell.SpellCastResult;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.RegexUtil;
import com.nisovin.magicspells.util.Util;

public class Subspell {

	private static Random random = new Random();
	
	private String spellName;
	private Spell spell;
	private CastMode mode = CastMode.PARTIAL;
	private float subPower = 1F;
	private int delay = 0;
	private double chance = -1D;
	
	private boolean isTargetedEntity = false;
	private boolean isTargetedLocation = false;
	private boolean isTargetedEntityFromLocation = false;
	
	private static final Pattern BARE_POWER_PATTERN = Pattern.compile("^[0-9]+\\.[0-9]+$");
	
	// spell-name (mode=hard|full|partial|direct,power=[subpower],delay=[delay],chance=[chance])|(hard)|(full)|(partial)|(direct)
	public Subspell(String data) {
		String[] split = data.split("\\(", 2);
		
		this.spellName = split[0].trim();
		
		if (split.length > 1) {
			split[1] = split[1].trim();
			if (split[1].endsWith(")")) split[1] = split[1].substring(0, split[1].length() - 1);
			String[] args = Util.splitParams(split[1]);
			for (String arg : args) {
				if (arg.contains("=")) {
					String[] keyval = arg.split("=");
					if (keyval[0].equalsIgnoreCase("mode")) {
						// TODO replace this with the name mapping
						if (keyval[1].equalsIgnoreCase("hard")) {
							this.mode = CastMode.HARD;
						} else if (keyval[1].equalsIgnoreCase("full")) {
							this.mode = CastMode.FULL;
						} else if (keyval[1].equalsIgnoreCase("partial")) {
							this.mode = CastMode.PARTIAL;
						} else if (keyval[1].equalsIgnoreCase("direct")) {
							this.mode = CastMode.DIRECT;
						}
					} else if (keyval[0].equalsIgnoreCase("power")) {
						try {
							this.subPower = Float.parseFloat(keyval[1]);
						} catch (NumberFormatException e) {
							DebugHandler.debugNumberFormat(e);
						}
					} else if (keyval[0].equalsIgnoreCase("delay")) {
						try {
							this.delay = Integer.parseInt(keyval[1]);
						} catch (NumberFormatException e) {
							DebugHandler.debugNumberFormat(e);
						}
					} else if (keyval[0].equalsIgnoreCase("chance")) {
						try {
							this.chance = Double.parseDouble(keyval[1]) / 100D;
						} catch (NumberFormatException e) {
							DebugHandler.debugNumberFormat(e);
						}
					}
					// TODO replace this with the name mapping
				} else if (arg.equalsIgnoreCase("hard")) {
					this.mode = CastMode.HARD;
				} else if (arg.equalsIgnoreCase("full")) {
					this.mode = CastMode.FULL;
				} else if (arg.equalsIgnoreCase("partial")) {
					this.mode = CastMode.PARTIAL;
				} else if (arg.equalsIgnoreCase("direct")) {
					this.mode = CastMode.DIRECT;
				} else if (RegexUtil.matches(RegexUtil.SIMPLE_INT_PATTERN, arg)) {  //TODO include this part in the comments
					this.delay = Integer.parseInt(arg);
				} else if (RegexUtil.matches(BARE_POWER_PATTERN, arg)) { //TODO include this part in the comments
					this.subPower = Float.parseFloat(arg);
				}
			}			
		}
	}
	
	public boolean process() {
		this.spell = MagicSpells.getSpellByInternalName(spellName);
		if (this.spell != null) {
			this.isTargetedEntity = this.spell instanceof TargetedEntitySpell;
			this.isTargetedLocation = this.spell instanceof TargetedLocationSpell;
			this.isTargetedEntityFromLocation = this.spell instanceof TargetedEntityFromLocationSpell;
		}
		return this.spell != null;
	}
	
	public Spell getSpell() {
		return this.spell;
	}
	
	public boolean isTargetedEntitySpell() {
		return this.isTargetedEntity;
	}
	
	public boolean isTargetedLocationSpell() {
		return this.isTargetedLocation;
	}
	
	public boolean isTargetedEntityFromLocationSpell() {
		return this.isTargetedEntityFromLocation;
	}
	
	public PostCastAction cast(final Player player, final float power) {
		if (this.chance > 0 && this.chance < 1) {
			if (random.nextDouble() > this.chance) return PostCastAction.ALREADY_HANDLED;
		}
		if (this.delay <= 0) return castReal(player, power);
		MagicSpells.scheduleDelayedTask(() -> castReal(player, power), delay);
		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	PostCastAction castReal(Player player, float power) {
		if ((this.mode == CastMode.HARD || this.mode == CastMode.FULL) && player != null) {
			return this.spell.cast(player, power * this.subPower, null).action;
		}
		
		if (this.mode == CastMode.PARTIAL) {
			SpellCastEvent event = new SpellCastEvent(this.spell, player, SpellCastState.NORMAL, power * this.subPower, null, 0, null, 0);
			EventUtil.call(event);
			if (!event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
				PostCastAction act = this.spell.castSpell(player, SpellCastState.NORMAL, event.getPower(), null);
				EventUtil.call(new SpellCastedEvent(this.spell, player, SpellCastState.NORMAL, event.getPower(), null, 0, null, act));
				return act;
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		
		return this.spell.castSpell(player, SpellCastState.NORMAL, power * this.subPower, null);
	}
	
	public boolean castAtEntity(final Player player, final LivingEntity target, final float power) {
		if (this.delay <= 0) return castAtEntityReal(player, target, power);
		MagicSpells.scheduleDelayedTask(() -> castAtEntityReal(player, target, power), delay);
		return true;
	}
	
	boolean castAtEntityReal(Player player, LivingEntity target, float power) {
		boolean ret = false;
		if (isTargetedEntity) {
			if (this.mode == CastMode.HARD && player != null) {
				SpellCastResult result = this.spell.cast(player, power, null); // the null is just no args
				return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
			} else if (this.mode == CastMode.FULL && player != null) {
				boolean success = false;
				SpellCastEvent spellCast = this.spell.preCast(player, power * this.subPower, null); // the null is just no args
				if (spellCast != null && spellCast.getSpellCastState() == SpellCastState.NORMAL) {
					success = ((TargetedEntitySpell)this.spell).castAtEntity(player, target, spellCast.getPower());
					this.spell.postCast(spellCast, success ? PostCastAction.HANDLE_NORMALLY : PostCastAction.ALREADY_HANDLED);
				}
				return success;
			} else if (this.mode == CastMode.PARTIAL) {
				SpellCastEvent event = new SpellCastEvent(this.spell, player, SpellCastState.NORMAL, power * this.subPower /*power*/, null /*args*/, 0, null /*reagents*/, 0);
				EventUtil.call(event);
				if (!event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
					if (player != null) {
						ret = ((TargetedEntitySpell)this.spell).castAtEntity(player, target, event.getPower());
					} else {
						ret = ((TargetedEntitySpell)this.spell).castAtEntity(target, event.getPower());
					}
					if (ret) EventUtil.call(new SpellCastedEvent(this.spell, player, SpellCastState.NORMAL, event.getPower(), null /*args*/, 0, null /*reagents*/, PostCastAction.HANDLE_NORMALLY));
				}
			} else {
				if (player != null) {
					ret = ((TargetedEntitySpell)this.spell).castAtEntity(player, target, power * this.subPower);
				} else {
					ret = ((TargetedEntitySpell)this.spell).castAtEntity(target, power * this.subPower);
				}
			}
		} else if (this.isTargetedLocation) {
			castAtLocationReal(player, target.getLocation(), power);
		}
		return ret;
	}
	
	public boolean castAtLocation(final Player player, final Location target, final float power) {
		if (this.delay <= 0) return castAtLocationReal(player, target, power);
		MagicSpells.scheduleDelayedTask(() -> castAtLocationReal(player, target, power), delay);
		return true;
	}
	
	boolean castAtLocationReal(Player player, Location target, float power) {
		boolean ret = false;
		if (this.isTargetedLocation) {
			if (this.mode == CastMode.HARD && player != null) {
				SpellCastResult result = this.spell.cast(player, power, null);
				return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
			} else if (this.mode == CastMode.FULL && player != null) {
				boolean success = false;
				SpellCastEvent spellCast = this.spell.preCast(player, power * this.subPower, null);
				if (spellCast != null && spellCast.getSpellCastState() == SpellCastState.NORMAL) {
					success = ((TargetedLocationSpell)this.spell).castAtLocation(player, target, spellCast.getPower());
					this.spell.postCast(spellCast, success ? PostCastAction.HANDLE_NORMALLY : PostCastAction.ALREADY_HANDLED);
				}
				return success;
			} else if (this.mode == CastMode.PARTIAL) {
				SpellCastEvent event = new SpellCastEvent(this.spell, player, SpellCastState.NORMAL, power * this.subPower, null, 0, null, 0);
				EventUtil.call(event);
				if (!event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
					if (player != null) {
						ret = ((TargetedLocationSpell)this.spell).castAtLocation(player, target, event.getPower());
					} else {
						ret = ((TargetedLocationSpell)this.spell).castAtLocation(target, event.getPower());
					}
					if (ret) EventUtil.call(new SpellCastedEvent(this.spell, player, SpellCastState.NORMAL, event.getPower(), null, 0, null, PostCastAction.HANDLE_NORMALLY));
				}
			} else {
				if (player != null) {
					ret = ((TargetedLocationSpell)this.spell).castAtLocation(player, target, power * this.subPower);
				} else {
					ret = ((TargetedLocationSpell)this.spell).castAtLocation(target, power * this.subPower);
				}
			}
		}
		return ret;
	}
	
	public boolean castAtEntityFromLocation(final Player player, final Location from, final LivingEntity target, final float power) {
		if (this.delay <= 0) return castAtEntityFromLocationReal(player, from, target, power);
		MagicSpells.scheduleDelayedTask(() -> castAtEntityFromLocationReal(player, from, target, power), delay);
		return true;
	}
	
	boolean castAtEntityFromLocationReal(Player player, Location from, LivingEntity target, float power) {
		boolean ret = false;
		if (this.isTargetedEntityFromLocation) {
			if (this.mode == CastMode.HARD && player != null) {
				SpellCastResult result = this.spell.cast(player, power, MagicSpells.NULL_ARGS);
				return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
			} else if (this.mode == CastMode.FULL && player != null) {
				boolean success = false;
				SpellCastEvent spellCast = this.spell.preCast(player, power * this.subPower, MagicSpells.NULL_ARGS);
				if (spellCast != null && spellCast.getSpellCastState() == SpellCastState.NORMAL) {
					success = ((TargetedEntityFromLocationSpell)this.spell).castAtEntityFromLocation(player, from, target, spellCast.getPower());
					this.spell.postCast(spellCast, success ? PostCastAction.HANDLE_NORMALLY : PostCastAction.ALREADY_HANDLED);
				}
				return success;
			} else if (this.mode == CastMode.PARTIAL) {
				SpellCastEvent event = new SpellCastEvent(this.spell, player, SpellCastState.NORMAL, power * this.subPower, null, 0, null, 0);
				EventUtil.call(event);
				if (!event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
					if (player != null) {
						ret = ((TargetedEntityFromLocationSpell)this.spell).castAtEntityFromLocation(player, from, target, event.getPower());
					} else {
						ret = ((TargetedEntityFromLocationSpell)this.spell).castAtEntityFromLocation(from, target, event.getPower());
					}
					if (ret) EventUtil.call(new SpellCastedEvent(this.spell, player, SpellCastState.NORMAL, event.getPower(), null, 0, null, PostCastAction.HANDLE_NORMALLY));
				}
			} else {
				if (player != null) {
					ret = ((TargetedEntityFromLocationSpell)this.spell).castAtEntityFromLocation(player, from, target, power * this.subPower);
				} else {
					ret = ((TargetedEntityFromLocationSpell)this.spell).castAtEntityFromLocation(from, target, power * this.subPower);
				}
			}
		}
		return ret;
	}
	
	// TODO this should be in charge of determining the mode from a string
	// TODO move this to its own class
	public enum CastMode {
		
		HARD,
		FULL,
		PARTIAL,
		DIRECT
		;
		
		private static Map<String, CastMode> nameMap = new HashMap<>();
		
		public static CastMode getFromString(String label) {
			return nameMap.get(label.toLowerCase());
		}
		
		static {
			for (CastMode mode : CastMode.values()) {
				nameMap.put(mode.name().toLowerCase(), mode);
			}
		}
		
	}
	
}
