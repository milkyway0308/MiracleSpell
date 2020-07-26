package com.nisovin.magicspells.materials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Objects;

public class MagicUnknownMaterial extends MagicMaterial {
	
	int type;
	short data;
	
	public MagicUnknownMaterial(int type, short data) {
		this.type = type;
		this.data = data;
	}
	
	@Override
	public Material getMaterial() {
		return Material.getMaterial(this.type);
	}
	
	@Override
	public MaterialData getMaterialData() {
		if (this.data == (byte)this.data) return new MaterialData(this.type, (byte)this.data);
		return new MaterialData(this.type);
	}
	
	@Override
	public void setBlock(Block block, boolean applyPhysics) {
		if (this.data < 16) block.setTypeIdAndData(this.type, (byte)this.data, applyPhysics);
	}
	
	@Override
	public FallingBlock spawnFallingBlock(Location location) {
		return location.getWorld().spawnFallingBlock(location, getMaterial(), getMaterialData().getData());
	}
	
	@Override
	public ItemStack toItemStack(int quantity) {
		return new ItemStack(this.type, quantity, this.data);
	}
	
	@Override
	public boolean equals(MaterialData matData) {
		return matData.getItemTypeId() == this.type && matData.getData() == this.data;
	}
	
	@Override
	public boolean equals(ItemStack itemStack) {
		return itemStack.getTypeId() == this.type && itemStack.getDurability() == this.data;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.type,
			":",
			this.data
		);
	}
	
}
