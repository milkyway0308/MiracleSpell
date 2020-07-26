package com.nisovin.magicspells.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;

public class MessageBlocker {

	Set<String> blocking = Collections.synchronizedSet(new HashSet<String>());
	
	PacketListener packetListener;
	
	public MessageBlocker() {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		this.packetListener = new PacketListener();
		protocolManager.addPacketListener(this.packetListener);
	}
	
	public void addPlayer(Player player) {
		blocking.add(player.getName());
	}
	
	public void removePlayer(Player player) {
		blocking.remove(player.getName());
	}
	
	public void turnOff() {
		if (this.packetListener == null) return;
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.removePacketListener(this.packetListener);
		this.packetListener = null;
	}
	
	class PacketListener extends PacketAdapter {
		
		public PacketListener() {
			super(MagicSpells.plugin, PacketType.Play.Server.CHAT);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if (!blocking.contains(event.getPlayer().getName())) return;
			event.setCancelled(true);
		}
		
	}
	
}
