package com.nisovin.magicspells.materials;

import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Objects;

public class MagicUnknownAnyDataMaterial extends MagicUnknownMaterial {

	public MagicUnknownAnyDataMaterial(int type) {
		super(type, (short)0);
	}
	
	@Override
	public boolean equals(MaterialData matData) {
		return matData.getItemTypeId() == this.type;
	}
	
	@Override
	public boolean equals(ItemStack itemStack) {
		return itemStack.getTypeId() == this.type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.type,
			":*"
		);
	}

}
