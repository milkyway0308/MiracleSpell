package com.nisovin.magicspells.zones;

import com.nisovin.magicspells.util.compat.CompatBasics;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.nisovin.magicspells.MagicSpells;

public class NoMagicZoneResidence extends NoMagicZone {

	private String regionName;
	
	@Override
	public void initialize(ConfigurationSection config) {
		this.regionName = config.getString("region", "");
	}

	@Override
	public boolean inZone(Location location) {
		if (CompatBasics.pluginEnabled("Residence")) {
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(location);
			return res != null && res.getName().equalsIgnoreCase(this.regionName);
		}
		MagicSpells.error("Failed to access Residence region '" + this.regionName + '\'');
		return false;
	}

}
