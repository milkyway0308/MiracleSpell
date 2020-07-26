package com.nisovin.magicspells;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nisovin.magicspells.util.TimeUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nisovin.magicspells.Spell.PostCastAction;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellLearnEvent.LearnSource;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.IntMap;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.util.Util;

public class MagicXpHandler implements Listener {

	MagicSpells plugin;
	
	Map<String, String> schools = new HashMap<>();
	Map<String, IntMap<String>> xp = new HashMap<>();
	Set<String> dirty = new HashSet<>();
	Map<String, String> currentWorld = new HashMap<>();
	
	Map<String, List<Spell>> spellSchoolRequirements = new HashMap<>();

	boolean autoLearn;
	String strXpHeader;
	String strNoXp;
	
	public MagicXpHandler(MagicSpells plugin, MagicConfig config) {
		this.plugin = plugin;
		
		Set<String> keys = config.getKeys("general.magic-schools");
		if (keys != null) {
			for (String school : keys) {
				String name = config.getString("general.magic-schools." + school, null);
				if (name != null) schools.put(school.toLowerCase(), name);
			}
		}
		autoLearn = config.getBoolean("general.magic-xp-auto-learn", false);
		strXpHeader = config.getString("general.str-xp-header", null);
		strNoXp = config.getString("general.str-no-xp", null);
		
		for (Spell spell : MagicSpells.spells()) {
			Map<String, Integer> xpRequired = spell.getXpRequired();
			if (xpRequired != null) {
				for (String school : xpRequired.keySet()) {
					List<Spell> list = spellSchoolRequirements.computeIfAbsent(school.toLowerCase(), s -> new ArrayList<>());
					list.add(spell);
				}
			}
		}
		Util.forEachPlayerOnline(this::load);
		MagicSpells.scheduleRepeatingTask(this::saveAll, TimeUtil.TICKS_PER_MINUTE, TimeUtil.TICKS_PER_MINUTE);
		MagicSpells.registerEvents(this);
	}
	
	public void showXpInfo(Player player) {
		MagicSpells.sendMessage(strXpHeader, player, MagicSpells.NULL_ARGS);
		IntMap<String> playerXp = xp.get(player.getName());
		if (playerXp != null) {
			if (!playerXp.isEmpty()) {
				for (String school : playerXp.keySet()) {
					String schoolName = schools.get(school);
					if (schoolName != null) {
						String amt = NumberFormat.getInstance().format(playerXp.get(school));
						MagicSpells.sendMessage(schoolName + ": " + amt, player, MagicSpells.NULL_ARGS);
					}
				}
			} else {
				MagicSpells.sendMessage(strNoXp, player, MagicSpells.NULL_ARGS);
			}
		} else {
			MagicSpells.sendMessage(strNoXp, player, MagicSpells.NULL_ARGS);
		}
	}
	
	public int getXp(Player player, String school) {
		IntMap<String> playerXp = xp.get(player.getName());
		if (playerXp != null) {
			return playerXp.get(school.toLowerCase());
		}
		return 0;
	}
	
	@EventHandler
	public void onCast(SpellCastedEvent event) {
		if (event.getPostCastAction() == PostCastAction.ALREADY_HANDLED) return;
		if (event.getSpellCastState() != SpellCastState.NORMAL) return;
		
		final Map<String, Integer> xpGranted = event.getSpell().getXpGranted();
		if (xpGranted == null) return;

		// Get player xp
		IntMap<String> playerXp = xp.computeIfAbsent(event.getCaster().getName(), s -> new IntMap<>());
		
		// Grant xp
		// FIXME use entry set here
		for (String school : xpGranted.keySet()) {
			playerXp.increment(school.toLowerCase(), xpGranted.get(school));
		}
		dirty.add(event.getCaster().getName());
		
		if (autoLearn) {
			final Player player = event.getCaster();
			final Spell castedSpell = event.getSpell();
			// TODO use lambda
			MagicSpells.scheduleDelayedTask(new Runnable() {
				@Override
				public void run() {
					
					// Get spells to check if learned
					Set<Spell> toCheck = new HashSet<>();
					for (String school : xpGranted.keySet()) {
						List<Spell> list = spellSchoolRequirements.get(school.toLowerCase());
						if (list != null) {
							for (Spell spell : list) {
								toCheck.add(spell);
							}
						}
					}
					
					// Check for new learned spells
					if (!toCheck.isEmpty()) {
						boolean learned = false;
						Spellbook spellbook = MagicSpells.getSpellbook(player);
						for (Spell spell : toCheck) {
							if (!spellbook.hasSpell(spell, false) && spellbook.canLearn(spell)) {
								SpellLearnEvent evt = new SpellLearnEvent(spell, player, LearnSource.MAGIC_XP, castedSpell);
								EventUtil.call(evt);
								if (!evt.isCancelled()) {
									spellbook.addSpell(spell);
									MagicSpells.sendMessage(spell.getStrXpLearned(), player, MagicSpells.NULL_ARGS);
									learned = true;
								}
							}
						}
						if (learned) spellbook.save();
					}
					
				}
			}, 1);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		currentWorld.put(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
		dirty.remove(event.getPlayer().getName());
		load(event.getPlayer());
	}

	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent event) {
		if (!plugin.separatePlayerSpellsPerWorld) return;
		Player player = event.getPlayer();
		String playerName = player.getName();
		if (dirty.contains(playerName)) {
			save(player);
		}
		currentWorld.put(playerName, player.getWorld().getName());
		load(player);
		dirty.remove(playerName);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		if (dirty.contains(playerName)) save(player);
		xp.remove(playerName);
		dirty.remove(playerName);
		currentWorld.remove(playerName);
	}
	
	public void load(Player player) {
		File folder = new File(plugin.getDataFolder(), "xp");
		if (!folder.exists()) folder.mkdirs();
		if (plugin.separatePlayerSpellsPerWorld) {
			String world = currentWorld.get(player.getName());
			if (world == null) world = player.getWorld().getName();
			folder = new File(folder, world);
			if (!folder.exists()) folder.mkdirs();
		}
		String uuid = Util.getUniqueId(player);
		File file = new File(folder, uuid + ".txt");
		if (!file.exists()) {
			File file2 = new File(folder, player.getName().toLowerCase());
			if (file2.exists()) file2.renameTo(file);
		}
		if (file.exists()) {
			YamlConfiguration conf = new YamlConfiguration();
			try {
				conf.load(file);
				IntMap<String> playerXp = new IntMap<>();
				for (String school : conf.getKeys(false)) {
					playerXp.put(school.toLowerCase(), conf.getInt(school, 0));
				}
				xp.put(player.getName(), playerXp);
			} catch (Exception e) {
				MagicSpells.error("Error while loading player XP for player " + player.getName());
				MagicSpells.handleException(e);
			}
		}
	}
	
	public void saveAll() {
		for (String playerName : dirty) {
			Player player = PlayerNameUtils.getPlayerExact(playerName);
			if (player != null) {
				save(player);
			}
		}
		dirty.clear();
	}
	
	public void save(Player player) {
		String world = currentWorld.get(player.getName());
		if (world == null) world = player.getWorld().getName();

		File folder = new File(plugin.getDataFolder(), "xp");
		if (!folder.exists()) folder.mkdirs();
		if (plugin.separatePlayerSpellsPerWorld) {
			if (world == null) return;
			folder = new File(folder, world);
			if (!folder.exists()) folder.mkdirs();
		}
		File file = new File(folder, Util.getUniqueId(player) + ".txt");
		if (file.exists()) file.delete();
		
		YamlConfiguration conf = new YamlConfiguration();
		IntMap<String> playerXp = xp.get(player.getName());
		if (playerXp != null) {
			for (String school : playerXp.keySet()) {
				conf.set(school.toLowerCase(), playerXp.get(school));
			}
		}
		
		try {
			conf.save(file);
		} catch (Exception e) {
			MagicSpells.error("Error while saving player XP for player " + player);
			MagicSpells.handleException(e);
		}
	}
	
}
