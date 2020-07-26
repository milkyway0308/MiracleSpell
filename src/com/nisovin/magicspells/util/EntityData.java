package com.nisovin.magicspells.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.DyeColor;
import org.bukkit.Location;
// this should probably be kept as a star import for version safety
import org.bukkit.entity.*;

import com.nisovin.magicspells.MagicSpells;

public class EntityData {

	private EntityType entityType;
	private boolean flag = false;
	private int var1 = 0;
	private int var2 = 0;
	private int var3 = 0;
	
	private static final Pattern PATTERN_VILLAGER_PROFESSION_INT = Pattern.compile("^[0-5]$");
	private static final Pattern PATTERN_WOLF_COLLAR = Pattern.compile("[0-9a-fA-F]+");
	private static final Pattern PATTERN_HORSE_ARMOR_TYPE = Pattern.compile("^[0-9]+$");
	private static final Pattern PATTERN_OZELOT_TYPE_DIGIT = Pattern.compile("ozelot [0-3]");
	
	// TODO change this to use a config formatting instead with legacy support for strings temporarily here
	// TODO the new format should use properties which check if their targets are possible on this version of spigot
	public EntityData(String type) {		
		if (type.startsWith("baby ")) {
			flag = true;
			type = type.replace("baby ", "");
		}
		if (type.equalsIgnoreCase("human") || type.equalsIgnoreCase("player")) {
			type = "player";
		} else if (type.equalsIgnoreCase("wither skeleton")) {
			if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
				entityType = EntityType.WITHER_SKELETON;
			} else {
				entityType = EntityType.SKELETON;
			}
			type = "skeleton";
			flag = true;
		} else if (type.equalsIgnoreCase("zombie villager") || type.equalsIgnoreCase("villager zombie")) {
			if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
				entityType = EntityType.ZOMBIE_VILLAGER;
			}
			type = "zombie";
			var1 = 1;
		} else if (type.equalsIgnoreCase("powered creeper")) {
			type = "creeper";
			flag = true;
		} else if (type.toLowerCase().startsWith("villager ")) {
			String prof = type.toLowerCase().replace("villager ", "");
			if (RegexUtil.matches(PATTERN_VILLAGER_PROFESSION_INT, prof)) {
				var1 = Integer.parseInt(prof);
			} else if (prof.toLowerCase().startsWith("green")) {
				var1 = 5;
			} else {
				try {
					var1 = getProfessionId(Villager.Profession.valueOf(prof.toUpperCase()));
				} catch (Exception e) {
					MagicSpells.error("Invalid villager profession: " + prof);
				}
			}
			type = "villager";
		} else if (type.toLowerCase().endsWith(" villager")) {
			String prof = type.toLowerCase().replace(" villager", "");
			if (prof.toLowerCase().startsWith("green")) {
				var1 = 5;
			} else {
				try {
					var1 = getProfessionId(Villager.Profession.valueOf(prof.toUpperCase()));
				} catch (Exception e) {
					MagicSpells.error("Invalid villager profession: " + prof);
				}
			}
			type = "villager";
		} else if (type.toLowerCase().endsWith(" sheep")) {
			String color = type.toLowerCase().replace(" sheep", "");
			if (color.equalsIgnoreCase("random")) {
				var1 = -1;
			} else {
				try {
					DyeColor dyeColor = DyeColor.valueOf(color.toUpperCase().replace(" ", "_"));
					if (dyeColor != null) {
						var1 = dyeColor.getWoolData();
					}
				} catch (IllegalArgumentException e) {
					MagicSpells.error("Invalid sheep color: " + color);
				}
			}
			type = "sheep";
		} else if (type.toLowerCase().endsWith(" rabbit")) {
			String rabbitType = type.toLowerCase().replace(" rabbit", "");
			var1 = 0;
			if (rabbitType.equals("white")) {
				var1 = 1;
			} else if (rabbitType.equals("black")) {
				var1 = 2;
			} else if (rabbitType.equals("blackwhite")) {
				var1 = 3;
			} else if (rabbitType.equals("gold")) {
				var1 = 4;
			} else if (rabbitType.equals("saltpepper")) {
				var1 = 5;
			} else if (rabbitType.equals("killer")) {
				var1 = 99;
			}
			type = "rabbit";
		} else if (type.toLowerCase().startsWith("wolf ")) {
			String color = type.toLowerCase().replace("wolf ", "");
			if (color.equals("angry")) {
				var1 = -1;
			} else if (RegexUtil.matches(PATTERN_WOLF_COLLAR, color)) {
				var1 = Integer.parseInt(color, 16);
			}
			type = "wolf";
		} else if (type.toLowerCase().equalsIgnoreCase("saddled pig")) {
			var1 = 1;
			type = "pig";
		} else if (type.equalsIgnoreCase("irongolem")) {
			type = "villagergolem";
		} else if (type.equalsIgnoreCase("mooshroom")) {
			type = "mushroomcow";
		} else if (type.equalsIgnoreCase("magmacube")) {
			type = "lavaslime";
		} else if (type.toLowerCase().contains("ocelot")) {
			type = type.toLowerCase().replace("ocelot", "ozelot");
		} else if (type.equalsIgnoreCase("snowgolem")) {
			type = "snowman";
		} else if (type.equalsIgnoreCase("wither")) {
			type = "witherboss";
		} else if (type.equalsIgnoreCase("dragon")) {
			type = "enderdragon";
		} else if (type.toLowerCase().startsWith("block") || type.toLowerCase().startsWith("fallingblock")) {
			String data = type.split(" ")[1];
			if (data.contains(":")) {
				String[] subdata = data.split(":");
				var1 = Integer.parseInt(subdata[0]);
				var2 = Integer.parseInt(subdata[1]);
			} else {
				var1 = Integer.parseInt(data);
			}
			type = "fallingsand";
		} else if (type.toLowerCase().startsWith("item")) {
			String data = type.split(" ")[1];
			if (data.contains(":")) {
				String[] subdata = data.split(":");
				var1 = Integer.parseInt(subdata[0]);
				var2 = Integer.parseInt(subdata[1]);
			} else {
				var1 = Integer.parseInt(data);
			}
			type = "item";
		} else if (type.toLowerCase().contains("horse")) {
			List<String> data = new ArrayList<>(Arrays.asList(type.split(" ")));
			var1 = 0;
			var2 = 0;
			if (data.get(0).equalsIgnoreCase("horse")) {
				data.remove(0);
			} else if (data.size() >= 2 && data.get(1).equalsIgnoreCase("horse")) {
				String t = data.remove(0).toLowerCase();
				if (t.equals("donkey")) {
					var1 = 1;
					if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
						entityType = EntityType.DONKEY;
					}
				} else if (t.equals("mule")) {
					var1 = 2;
					if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
						entityType = EntityType.MULE;
					}
				} else if (t.equals("skeleton") || t.equals("skeletal")) {
					var1 = 4;
					if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
						entityType = EntityType.SKELETON_HORSE;
					}
				} else if (t.equals("zombie") || t.equals("undead")) {
					var1 = 3;
					if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
						entityType = EntityType.ZOMBIE_HORSE;
					}
				} else {
					var1 = 0;
					if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
						entityType = EntityType.HORSE;
					}
				}
				data.remove(0);
			}
			while (!data.isEmpty()) {
				String d = data.remove(0);
				if (RegexUtil.matches(PATTERN_HORSE_ARMOR_TYPE, d)) {
					var2 = Integer.parseInt(d);
				} else if (d.equalsIgnoreCase("iron")) {
					var3 = 1;
				} else if (d.equalsIgnoreCase("gold")) {
					var3 = 2;
				} else if (d.equalsIgnoreCase("diamond")) {
					var3 = 3;
				}
			}
			type = "entityhorse";
		} else if (type.equalsIgnoreCase("mule")) {
			var1 = 2;
			type = "entityhorse";
			if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
				entityType = EntityType.MULE;
			}
		} else if (type.equalsIgnoreCase("donkey")) {
			var1 = 1;
			type = "entityhorse";
		} else if (type.equalsIgnoreCase("elder guardian")) {
			if (V1_11EntityTypeHandler.newEntityTypesPresent()) {
				entityType = EntityType.ELDER_GUARDIAN;
			}
			flag = true;
			type = "guardian";
		}
		if (RegexUtil.matches(PATTERN_OZELOT_TYPE_DIGIT, type.toLowerCase())) {
			var1 = Integer.parseInt(type.split(" ")[1]);
			type = "ozelot";
		} else if (type.toLowerCase().equals("ozelot random") || type.toLowerCase().equals("random ozelot")) {
			var1 = -1;
			type = "ozelot";
		}
		if (type.equals("slime") || type.equals("lavaslime")) {
			var1 = 1;
		} else if (type.startsWith("slime") || type.startsWith("magmacube") || type.startsWith("lavaslime")) {
			String[] data = type.split(" ");
			type = data[0];
			if (type.equals("magmacube")) type = "lavaslime";
			var1 = Integer.parseInt(data[1]);
		}
		if (entityType == null) {
			if (type.equals("player")) {
				entityType = EntityType.PLAYER;
			} else {
				entityType = EntityType.fromName(type);
			}
		}
	}
	
	public EntityType getType() {
		return entityType;
	}
	
	public boolean getFlag() {
		return flag;
	}
	
	public int getVar1() {
		return var1;
	}
	
	public int getVar2() {
		return var2;
	}
	
	public int getVar3() {
		return var3;
	}
	
	public Entity spawn(Location loc) {
		
		Entity entity = loc.getWorld().spawnEntity(loc, entityType);
		if (entity instanceof Ageable && flag) {
			((Ageable)entity).setBaby();
		}
		if (entityType == EntityType.ZOMBIE) {
			((Zombie)entity).setBaby(flag);
			if (!V1_11EntityTypeHandler.newEntityTypesPresent()) {
				((Zombie)entity).setVillager(var1 == 1); // This is safe due to version checks
			}
		} else if (entityType == EntityType.SKELETON) {
			if (!V1_11EntityTypeHandler.newEntityTypesPresent()) {
				if (flag) {
					((Skeleton)entity).setSkeletonType(Skeleton.SkeletonType.WITHER); // This is safe due to version checks
				}
			}
		} else if (entityType == EntityType.CREEPER) {
			if (flag) {
				((Creeper)entity).setPowered(true);
			}
		} else if (entityType == EntityType.WOLF) {
			if (var1 == -1) {
				((Wolf)entity).setAngry(true);
			}
		} else if (entityType == EntityType.OCELOT) {
			if (var1 == 0) {
				((Ocelot)entity).setCatType(Ocelot.Type.WILD_OCELOT);
			} else if (var1 == 1) {
				((Ocelot)entity).setCatType(Ocelot.Type.BLACK_CAT);
			} else if (var1 == 2) {
				((Ocelot)entity).setCatType(Ocelot.Type.RED_CAT);
			} else if (var1 == 3) {
				((Ocelot)entity).setCatType(Ocelot.Type.SIAMESE_CAT);
			}
		} else if (entityType == EntityType.VILLAGER) {
			if (var1 == 0) {
				((Villager)entity).setProfession(Villager.Profession.FARMER);
			} else if (var1 == 1) {
				((Villager)entity).setProfession(Villager.Profession.LIBRARIAN);
			} else if (var1 == 2) {
				((Villager)entity).setProfession(Villager.Profession.PRIEST);
			} else if (var1 == 3) {
				((Villager)entity).setProfession(Villager.Profession.BLACKSMITH);
			} else if (var1 == 4) {
				((Villager)entity).setProfession(Villager.Profession.BUTCHER);
			}
		} else if (entityType == EntityType.SLIME) {
			((Slime)entity).setSize(var1);
		} else if (entityType == EntityType.MAGMA_CUBE) {
			((MagmaCube)entity).setSize(var1);
		} else if (entityType == EntityType.PIG) {
			if (var1 == 1) {
				((Pig)entity).setSaddle(true);
			}
		} else if (entityType == EntityType.SHEEP) {
			DyeColor c = DyeColor.getByWoolData((byte)var1);
			if (c != null) {
				((Sheep)entity).setColor(c);
			}
		} else if (entityType == EntityType.RABBIT) {
			/*if (var1 == 0) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.BROWN);
			} else if (var1 == 1) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.WHITE);
			} else if (var1 == 2) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.BLACK);
			} else if (var1 == 3) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.BLACK_AND_WHITE);
			} else if (var1 == 4) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.GOLD);
			} else if (var1 == 5) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.SALT_AND_PEPPER);
			} else if (var1 == 99) {
				((Rabbit)entity).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
			}*/
		} else if (entityType == EntityType.GUARDIAN) {
			if (!V1_11EntityTypeHandler.newEntityTypesPresent()) {
				if (flag) {
					((Guardian)entity).setElder(true); // This is safe due to version checks
				}
			}
		} else if (entityType == EntityType.HORSE) {
			if (!V1_11EntityTypeHandler.newEntityTypesPresent()) {
				if (var1 == 0) {
					((Horse)entity).setVariant(Horse.Variant.HORSE); // This is safe due to version checks
				} else if (var1 == 1) {
					((Horse)entity).setVariant(Horse.Variant.DONKEY); // This is safe due to version checks
				} else if (var1 == 2) {
					((Horse)entity).setVariant(Horse.Variant.MULE); // This is safe due to version checks
				} else if (var1 == 3) {
					((Horse)entity).setVariant(Horse.Variant.UNDEAD_HORSE); // This is safe due to version checks
				} else if (var1 == 4) {
					((Horse)entity).setVariant(Horse.Variant.SKELETON_HORSE); // This is safe due to version checks
				}
			}
		}		
		return entity;
	}
	
	private static int getProfessionId(Villager.Profession prof) {
		switch (prof) {
		case FARMER:
			return 0;
		case LIBRARIAN:
			return 1;
		case PRIEST:
			return 2;
		case BLACKSMITH:
			return 3;
		case BUTCHER:
			return 4;
		default:
			return 0;
		}
	}
	
}
