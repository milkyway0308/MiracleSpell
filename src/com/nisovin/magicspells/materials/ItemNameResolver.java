package com.nisovin.magicspells.materials;

import java.util.Random;


public interface ItemNameResolver {

	Random rand = new Random();

	@Deprecated
	ItemTypeAndData resolve(String string);
	
	MagicMaterial resolveItem(String string);
	
	MagicMaterial resolveBlock(String string);
	
	class ItemTypeAndData {
		
		public int id = 0;
		public short data = 0;
		
	}
	
}
