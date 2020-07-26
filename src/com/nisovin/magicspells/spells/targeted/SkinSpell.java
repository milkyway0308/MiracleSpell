package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;

public class SkinSpell extends TargetedSpell {
	
	private String texture = null;
	private String signature = null;
	
	public SkinSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		texture = getConfigString("texture", null);
		signature = getConfigString("signature", null);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
			if (targetInfo != null && targetInfo.getTarget() != null) {
				MagicSpells.getVolatileCodeHandler().setSkin(targetInfo.getTarget(), texture, signature);
				return PostCastAction.HANDLE_NORMALLY;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
