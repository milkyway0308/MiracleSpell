package com.nisovin.magicspells.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class MagicSpellsBlockBreakEvent extends BlockBreakEvent implements IMagicSpellsCompatEvent {

	public MagicSpellsBlockBreakEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}

}
