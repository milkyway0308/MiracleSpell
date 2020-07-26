package com.nisovin.magicspells.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nisovin.magicspells.MagicSpells;

public class MagicConfig {
	
	private static final FilenameFilter FILENAME_FILTER = (File dir, String name) -> name.startsWith("spell") && name.endsWith(".yml");
	
	private YamlConfiguration mainConfig;
	
	public MagicConfig(File file) {
		this(MagicSpells.plugin);
	}
	
	/*
	 * general outline of the format of this config (WIP)
	 * general:
	 *     enable-volatile-features: true
	 *     debug: false
	 *     debug-null: true
	 *     debug-number-format: true
	 *     debug-level: 3
	 *     enable-error-logging: true
	 *     enable-profiling: false
	 *     text-color: ChatColor.DARK_AQUA.getChar()
	 *     broadcast-range: 20
	 *     ops-have-all-spells: true
	 *     default-all-perms-false: false
	 *     ignore-grant-perms: false
	 *     ignore-cast-perms: false
	 *     separate-player-spells-per-world: false
	 *     allow-cycle-to-no-spell: false
	 *     always-show-message-on-cycle: false
	 *     only-cycle-to-castable-spells: true
	 *     spell-icon-slot: -1
	 *     allow-cast-with-fist: false
	 *     cast-with-left-click: true
	 *     cast-with-right-click: false
	 *     ignore-default-bindings: false
	 *     ignore-cast-item-enchants: true
	 *     ignore-cast-item-names: false
	 *     ignore-cast-item-name-colors: false
	 *     check-world-pvp-flag: true
	 *     check-scoreboard-teams: false
	 *     show-str-cost-on-missing-reagents: true
	 *     los-transparent-blocks: new ArrayList<Byte>()
	 *     ignore-cast-item-durability: new ArrayList<Integer>()
	 *     global-cooldown: 500
	 *     cast-on-animate: false
	 *     use-exp-bar-as-cast-time-bar: true
	 *     cooldowns-persist-through-reload: true
	 *     entity-names:
	 *     sound-on-cooldown: null
	 *     sound-missing-reagents: null
	 *     str-cast-usage: "Usage: /cast <spell>. Use /cast list to see a list of spells."
	 *     str-unknown-spell: "You do not know a spell with that name."
	 *     str-spell-change: "You are now using the %s spell."
	 *     str-spell-change-empty: "You are no longer using a spell."
	 *     str-on-cooldown: "That spell is on cooldown."
	 *     str-missing-reagents: "You do not have the reagents for that spell."
	 *     str-cant-cast: "You can't cast that spell right now."
	 *     str-cant-bind: "You cannot bind that spell to that item."
	 *     str-wrong-world: "You cannot cast that spell here."
	 *     console-name: "Admin"
	 *     str-xp-auto-learned: "You have learned the %s spell!"
	 *     buff-check-interval: 0
	 *     ops-ignore-reagents: true
	 *     ops-ignore-cooldowns: true
	 *     ops-ignore-cast-times: true
	 *     hide-predefined-items-tooltips: false
	 *     enable-magic-xp: false
	 *     enable-dance-casting: true
	 *     enable-logging: false
	 *     enable-tab-completion: true
	 *     
	 *     
	 *     predefined-items:
	 *     variables:
	 *     modifiers:
	 * mana:
	 *     enable-mana-system: false
	 *     mana-potion-cooldown: 30
	 *     str-mana-potion-on-cooldown: "You cannot use another mana potion yet."
	 *     mana-potions: null
	 *     
	 * spells:
	 * no-magic-zones:
	 */
	public MagicConfig(MagicSpells plugin) {
		try {
			File folder = plugin.getDataFolder();
			File file = new File(folder, "config.yml");
			
			// Load main config
			this.mainConfig = new YamlConfiguration();
			if (file.exists()) this.mainConfig.load(file);
			if (!this.mainConfig.contains("general")) this.mainConfig.createSection("general");
			if (!this.mainConfig.contains("mana")) this.mainConfig.createSection("mana");
			if (!this.mainConfig.contains("spells")) this.mainConfig.createSection("spells");
			
			// Load general
			File generalConfigFile = new File(folder, "general.yml");
			if (generalConfigFile.exists()) {
				YamlConfiguration generalConfig = new YamlConfiguration();
				try {
					generalConfig.load(generalConfigFile);
					Set<String> keys = generalConfig.getKeys(true);
					for (String key : keys) {
						this.mainConfig.set("general." + key, generalConfig.get(key));
					}
				} catch (Exception e) {
					MagicSpells.error("Error loading config file general.yml");
					MagicSpells.handleException(e);
				}
			}
			
			// Load mana
			File manaConfigFile = new File(folder, "mana.yml");
			if (manaConfigFile.exists()) {
				YamlConfiguration manaConfig = new YamlConfiguration();
				try {
					manaConfig.load(manaConfigFile);
					Set<String> keys = manaConfig.getKeys(true);
					for (String key : keys) {
						this.mainConfig.set("mana." + key, manaConfig.get(key));
					}
				} catch (Exception e) {
					MagicSpells.error("Error loading config file mana.yml");
					MagicSpells.handleException(e);
				}
			}
			
			// Load no magic zones
			File zonesConfigFile = new File(folder, "zones.yml");
			if (zonesConfigFile.exists()) {
				YamlConfiguration zonesConfig = new YamlConfiguration();
				try {
					zonesConfig.load(zonesConfigFile);
					Set<String> keys = zonesConfig.getKeys(true);
					for (String key : keys) {
						this.mainConfig.set("no-magic-zones." + key, zonesConfig.get(key));
					}
				} catch (Exception e) {
					MagicSpells.error("Error loading config file zones.yml");
					MagicSpells.handleException(e);
				}
			}
			
			// Load spell configs
			for (File spellConfigFile : folder.listFiles(FILENAME_FILTER)) {
				YamlConfiguration spellConfig = new YamlConfiguration();
				try {
					spellConfig.load(spellConfigFile);
					Set<String> keys = spellConfig.getKeys(false);
					
					// TODO this should be refactored to allow registration of additional "special sections"
					for (String key : keys) {
						if (key.equals("predefined-items")) {
							ConfigurationSection sec = this.mainConfig.getConfigurationSection("general.predefined-items");
							if (sec == null) sec = this.mainConfig.createSection("general.predefined-items");
							for (String itemKey : spellConfig.getConfigurationSection("predefined-items").getKeys(false)) {
								sec.set(itemKey, spellConfig.get("predefined-items." + itemKey));
							}
						} else if (key.equals("variables")) {
							ConfigurationSection sec = this.mainConfig.getConfigurationSection("general.variables");
							if (sec == null) sec = this.mainConfig.createSection("general.variables");
							for (String itemKey : spellConfig.getConfigurationSection("variables").getKeys(false)) {
								sec.set(itemKey, spellConfig.get("variables." + itemKey));
							}
						} else if (key.equals("modifiers")) {
							ConfigurationSection sec = this.mainConfig.getConfigurationSection("general.modifiers");
							if (sec == null) sec = this.mainConfig.createSection("general.modifiers");
							for (String modifierKey : spellConfig.getConfigurationSection("modifiers").getKeys(false)) {
								sec.set(modifierKey, spellConfig.get("modifiers." + modifierKey));
							}
						} else {
							this.mainConfig.set("spells." + key, spellConfig.get(key));
						}
					}
				} catch (Exception e) {
					MagicSpells.error("Error loading config file " + spellConfigFile.getName());
					MagicSpells.handleException(e);
				}
			}
			
			// Load mini configs
			File spellConfigsFolder = new File(folder, "spellconfigs");
			if (spellConfigsFolder.exists()) loadSpellConfigs(spellConfigsFolder);
		} catch (Exception ex) {
			MagicSpells.handleException(ex);
		}
	}
	
	private void loadSpellConfigs(File folder) {
		YamlConfiguration conf;
		String name;
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				// Recurse into folders
				loadSpellConfigs(file);
			} else if (file.getName().endsWith(".yml")) {
				name = file.getName().replace(".yml", "");
				conf = new YamlConfiguration();
				try {
					conf.load(file);
					for(String key : conf.getKeys(false)) {
						this.mainConfig.set("spells." + name + '.' + key, conf.get(key));
					}
				} catch (Exception e) {
					MagicSpells.error("Error reading spell config file: " + file.getName());
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isLoaded() {
		return this.mainConfig.contains("general") && this.mainConfig.contains("spells");
	}
	
	public boolean contains(String path) {
		return this.mainConfig.contains(path);
	}
	
	public int getInt(String path, int def) {
		return this.mainConfig.getInt(path, def);
	}
	
	public long getLong(String path, long def) {
		return this.mainConfig.getLong(path, def);
	}
	
	public double getDouble(String path, double def) {
		if (this.mainConfig.contains(path) && this.mainConfig.isInt(path)) return this.mainConfig.getInt(path);
		return this.mainConfig.getDouble(path, def);
	}
	
	public boolean getBoolean(String path, boolean def) {		
		return this.mainConfig.getBoolean(path, def);
	}
	
	public boolean isString(String path) {
		return this.mainConfig.contains(path) && this.mainConfig.isString(path);
	}
	
	public String getString(String path, String def) {
		if (!this.mainConfig.contains(path)) return def;
		return this.mainConfig.get(path).toString();
	}
	
	public boolean isList(String path) {
		return this.mainConfig.contains(path) && this.mainConfig.isList(path);
	}
	
	public List<Integer> getIntList(String path, List<Integer> def) {
		if (!this.mainConfig.contains(path)) return def;
		List<Integer> l = this.mainConfig.getIntegerList(path);
		if (l != null) return l;
		return def;
	}
	
	public List<Byte> getByteList(String path, List<Byte> def) {
		if (!this.mainConfig.contains(path)) return def;
		List<Byte> l = this.mainConfig.getByteList(path);
		if (l != null) return l;
		return def;
	}
	
	public List<String> getStringList(String path, List<String> def) {
		if (!this.mainConfig.contains(path)) return def;
		List<String> l = this.mainConfig.getStringList(path);
		if (l != null) return l;
		return def;
	}
	
	public List<?> getList(String path, List<?> def) {
		if (!this.mainConfig.contains(path)) return def;
		List<?> l = this.mainConfig.getList(path);
		if (l != null) return l;
		return def;
	}
	
	public Set<String> getKeys(String path) {
		if (!this.mainConfig.contains(path)) return null;
		if (!this.mainConfig.isConfigurationSection(path)) return null;
		return this.mainConfig.getConfigurationSection(path).getKeys(false);
	}
	
	public boolean isSection(String path) {
		return this.mainConfig.contains(path) && this.mainConfig.isConfigurationSection(path);
	}
	
	public ConfigurationSection getSection(String path) {
		if (this.mainConfig.contains(path)) return this.mainConfig.getConfigurationSection(path);
		return null;
	}
	
	public Set<String> getSpellKeys() {
		if (this.mainConfig == null) return null;
		if (!this.mainConfig.contains("spells")) return null;
		if (!this.mainConfig.isConfigurationSection("spells")) return null;
		return this.mainConfig.getConfigurationSection("spells").getKeys(false);
	}
	
}
