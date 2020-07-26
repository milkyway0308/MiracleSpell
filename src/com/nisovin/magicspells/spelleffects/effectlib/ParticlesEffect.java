package com.nisovin.magicspells.spelleffects.effectlib;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.ParticleEffect;

// the way to tell effectlib about this class is by calling it
// "com.nisovin.magicspells.spelleffects.effectlib.ParticlesEffect"
public class ParticlesEffect extends Effect {

	ParticleEffect particle = ParticleEffect.REDSTONE;
	
	public ParticlesEffect(EffectManager effectManager) {
		super(effectManager);
	}

	@Override
	public void onRun() {
		display(particle, getLocation());
	}

}
