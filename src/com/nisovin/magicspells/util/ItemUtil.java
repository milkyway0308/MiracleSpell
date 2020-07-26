package com.nisovin.magicspells.util;


import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.util.wrappers.CraftItemStackWrapper;
import com.nisovin.magicspells.util.wrappers.NBTTagCompoundWrapper;
import com.nisovin.magicspells.util.wrappers.NBTTagListWrapper;
import com.nisovin.magicspells.util.wrappers.NMSItemStackWrapper;

public class ItemUtil {
	
	public ItemStack setUnbreakable(ItemStack item) {
		if (!CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			try {
				item = (ItemStack) CraftItemStackWrapper.asCraftCopyMethod.invoke(null, item);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		Object tag = getTag(item);
		if (tag == null) tag = NBTTagCompoundWrapper.newInstance();
		try {
			NBTTagCompoundWrapper.setByteMethod.invoke(tag, "Unbreakable", (byte)1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return setTag(item, tag);
	}
	
	private static Object getTag(ItemStack item) {
		if (CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			try {
				return NMSItemStackWrapper.getTagMethod.invoke(CraftItemStackWrapper.craftItemStackHandleField.get(item));
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param item
	 * @param tag NBTTagCompound as an {@link Object}
	 * @return
	 */
	private static ItemStack setTag(ItemStack item, Object tag) {
		Object craftItem = null;
		
		if (CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			craftItem = item;
		} else {
			try {
				craftItem = CraftItemStackWrapper.asCraftCopyMethod.invoke(null, item);
			} catch (Exception e) {
				// No op
			}
		}
		
		Object nmsItem = null;
		try {
			nmsItem = CraftItemStackWrapper.craftItemStackHandleField.get(item);
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
		}
		if (nmsItem == null) {
			try {
				nmsItem = CraftItemStackWrapper.asNMSCopyMethod.invoke(null, craftItem);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		
		if (nmsItem != null) {
			try {
				NMSItemStackWrapper.setTagMethod.invoke(nmsItem, tag);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
			try {
				CraftItemStackWrapper.craftItemStackHandleField.set(craftItem, nmsItem);;
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		
		return (ItemStack) craftItem;
	}
	
	public ItemStack addFakeEnchantment(ItemStack item) {
		if (!CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			try {
				item = (ItemStack) CraftItemStackWrapper.asCraftCopyMethod.invoke(null, item);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		Object tag = getTag(item);		
		if (tag == null) tag = NBTTagCompoundWrapper.newInstance();
		try {
			if (!(Boolean)NBTTagCompoundWrapper.hasKeyMethod.invoke(tag, "ench")) {
				NBTTagCompoundWrapper.setMethod.invoke(tag, "ench", NBTTagListWrapper.newInstance());
			}
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
		}		
		return setTag(item, tag);
	}
	
	public ItemStack addAttributes(ItemStack item, String[] names, String[] types, double[] amounts, int[] operations) {
		if (!CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			//item = CraftItemStack.asCraftCopy(item);
			try {
				item = (ItemStack) CraftItemStackWrapper.asCraftCopyMethod.invoke(null, item);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		Object tag = getTag(item);
		
		Object list = NBTTagListWrapper.newInstance();
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null) {
				Object attr = NBTTagCompoundWrapper.newInstance();
				try {
					NBTTagCompoundWrapper.setStringMethod.invoke(attr, "Name", names[i]);
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				try {
					NBTTagCompoundWrapper.setStringMethod.invoke(attr, "AttributeName", names[i]);
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				try {
					NBTTagCompoundWrapper.setDoubleMethod.invoke(attr, "Amount", amounts[i]);
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				try {
					NBTTagCompoundWrapper.setIntMethod.invoke(attr, "Operation", operations[i]);
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				UUID uuid = UUID.randomUUID();
				try {
					NBTTagCompoundWrapper.setLongMethod.invoke(attr, "UUIDLeast", uuid.getLeastSignificantBits());
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				try {
					NBTTagCompoundWrapper.setLongMethod.invoke(attr, "UUIDMost", uuid.getMostSignificantBits());
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
				//list.add(attr);
				try {
					NBTTagListWrapper.addMethod.invoke(list, attr);
				} catch (Exception e) {
					DebugHandler.debugGeneral(e);
				}
			}
		}
		
		//tag.set("AttributeModifiers", list);
		try {
			NBTTagCompoundWrapper.setMethod.invoke(tag, "AttributeModifiers", list);
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
		}
		setTag(item, tag);
		return item;
	}
	
	public ItemStack hideTooltipCrap(ItemStack item) {
		if (!CraftItemStackWrapper.targetClass.isAssignableFrom(item.getClass())) {
			try {
				item = (ItemStack) CraftItemStackWrapper.asCraftCopyMethod.invoke(null, item);
			} catch (Exception e) {
				DebugHandler.debugGeneral(e);
			}
		}
		Object tag = getTag(item);
		if (tag == null) tag = NBTTagCompoundWrapper.newInstance();
		try {
			NBTTagCompoundWrapper.setIntMethod.invoke(tag, "HideFlags", 63);
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
		}
		setTag(item, tag);
		return item;
	}
	
}
