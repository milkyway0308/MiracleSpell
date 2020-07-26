package com.nisovin.magicspells.volatilecode;

import org.bukkit.Location;

import com.nisovin.magicspells.MagicSpells;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.ParticleEffect;

public class VolatileCodeEffectLib extends VolatileCodeDisabled {

	private EffectManager effectManager;
	
	public VolatileCodeEffectLib() {
		effectManager = MagicSpells.plugin.effectManager;
	}
	
	@Override
	public void playParticleEffect(Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset) {
		ParticleEffect effect;
		if (name != null) {
			effect = ParticleEffect.fromName(name);
		} else {
			throw new NullPointerException("Particle name cannot be null");
		}
		
		if (effect == null) effect = ParticleEffect.valueOf(name);
		
		//ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount
		Location displayLocation = location.add(0, yOffset, 0);
		effect.display(null, displayLocation, null, radius, spreadHoriz, spreadVert, spreadHoriz, speed, count);
	}

}
