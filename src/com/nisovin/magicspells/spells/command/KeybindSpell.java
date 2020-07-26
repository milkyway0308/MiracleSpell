package com.nisovin.magicspells.spells.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class KeybindSpell extends CommandSpell {

	ItemStack wandItem;
	ItemStack defaultSpellIcon;
	
	private HashMap<String,Keybinds> playerKeybinds;
	
	public KeybindSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.wandItem = Util.getItemStackFromString(getConfigString("wand-item", "blaze_rod"));
		this.defaultSpellIcon = Util.getItemStackFromString(getConfigString("default-spell-icon", "redstone"));
		
		this.playerKeybinds = new HashMap<>();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		Util.forEachPlayerOnline(this::loadKeybinds);
	}

	private void loadKeybinds(Player player) {
		File file = new File(MagicSpells.plugin.getDataFolder(), "spellbooks" + File.separator + "keybinds-" + player.getName().toLowerCase() + ".txt");
		if (file.exists()) {
			try {
				Keybinds keybinds = new Keybinds(player);				
				YamlConfiguration conf = new YamlConfiguration();
				conf.load(file);
				for (String key : conf.getKeys(false)) {
					int slot = Integer.parseInt(key);
					String spellName = conf.getString(key);
					Spell spell = MagicSpells.getSpellByInternalName(spellName);
					if (spell != null) keybinds.setKeybind(slot, spell);
				}
				this.playerKeybinds.put(player.getName(), keybinds);
			} catch (Exception e) {
				MagicSpells.plugin.getLogger().severe("Failed to load player keybinds for " + player.getName());
				e.printStackTrace();
			}
		}
	}
	
	private void saveKeybinds(Keybinds keybinds) {		
		File file = new File(MagicSpells.plugin.getDataFolder(), "spellbooks" + File.separator + "keybinds-" + keybinds.player.getName().toLowerCase() + ".txt");
		YamlConfiguration conf = new YamlConfiguration();
		Spell[] binds = keybinds.keybinds;
		for (int i = 0; i < binds.length; i++) {
			if (binds[i] == null) continue;
			conf.set(i + "", binds[i].getInternalName());
		}
		try {
			conf.save(file);
		} catch (IOException e) {
			MagicSpells.plugin.getLogger().severe("Failed to save keybinds for " + keybinds.player.getName());
			e.printStackTrace();
		}
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args.length != 1) {
				// Fail
				player.sendMessage("invalid args");
				return PostCastAction.ALREADY_HANDLED;
			}
			
			Keybinds keybinds = this.playerKeybinds.computeIfAbsent(player.getName(), name -> new Keybinds(player));
			
			int slot = player.getInventory().getHeldItemSlot();
			ItemStack item = HandHandler.getItemInMainHand(player);
			
			if (args[0].equalsIgnoreCase("clear")) {
				keybinds.clearKeybind(slot);
				saveKeybinds(keybinds);
			} else if (args[0].equalsIgnoreCase("clearall")) {
				keybinds.clearKeybinds();
				saveKeybinds(keybinds);
			} else {
				if (item != null && item.getType() != Material.AIR) {
					// Fail
					player.sendMessage("not empty");
					return PostCastAction.ALREADY_HANDLED;
				}
				
				Spell spell = MagicSpells.getSpellbook(player).getSpellByName(args[0]);
				if (spell == null) {
					// Fail
					player.sendMessage("no spell");
					return PostCastAction.ALREADY_HANDLED;
				}
				
				keybinds.setKeybind(slot, spell);
				keybinds.select(slot);
				saveKeybinds(keybinds);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@EventHandler
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		Keybinds keybinds = this.playerKeybinds.get(event.getPlayer().getName());
		if (keybinds == null) return;
		keybinds.deselect(event.getPreviousSlot());
		keybinds.select(event.getNewSlot());
	}
	
	@EventHandler
	public void onAnimate(PlayerAnimationEvent event) {
		Keybinds keybinds = this.playerKeybinds.get(event.getPlayer().getName());
		if (keybinds == null) return;
		boolean casted = keybinds.castKeybind(event.getPlayer().getInventory().getHeldItemSlot());
		if (casted) event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		
		Keybinds keybinds = this.playerKeybinds.get(event.getPlayer().getName());
		if (keybinds == null) return;
		
		if (keybinds.hasKeybind(event.getPlayer().getInventory().getHeldItemSlot())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadKeybinds(event.getPlayer());
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		return null;
	}
	
	protected void sendFakeSlotUpdate(Player player, int slot, ItemStack item) {
		MagicSpells.getVolatileCodeHandler().sendFakeSlotUpdate(player, slot, item);
	}
	
	private class Keybinds {
		
		Player player;
		Spell[] keybinds;
		
		public Keybinds(Player player) {
			this.player = player;
			this.keybinds = new Spell[10];
		}
		
		public void deselect(int slot) {
			Spell spell = this.keybinds[slot];
			if (spell == null) return;
			ItemStack spellIcon = spell.getSpellIcon();
			if (spellIcon == null) spellIcon = defaultSpellIcon;
			//player.getInventory().setItem(slot, new ItemStack(spellIcon.getType(), 0, spellIcon.getDurability()));
			sendFakeSlotUpdate(this.player, slot, spellIcon);
		}
		
		public void select(int slot) {
			Spell spell = this.keybinds[slot];
			if (spell == null) return;
			sendFakeSlotUpdate(this.player, slot, wandItem);
		}
		
		public boolean hasKeybind(int slot) {
			return this.keybinds[slot] != null;
		}
		
		public boolean castKeybind(int slot) {
			Spell spell = this.keybinds[slot];
			if (spell == null) return false;
			spell.cast(this.player);
			return true;
		}
		
		public void setKeybind(int slot, Spell spell) {
			this.keybinds[slot] = spell;
		}
		
		public void clearKeybind(int slot) {
			this.keybinds[slot] = null;
			sendFakeSlotUpdate(this.player, slot, null);
		}
		
		public void clearKeybinds() {
			for (int i = 0; i < this.keybinds.length; i++) {
				if (this.keybinds[i] == null) continue;
				this.keybinds[i] = null;
				sendFakeSlotUpdate(this.player, i, null);
			}
		}
		
	}

}
