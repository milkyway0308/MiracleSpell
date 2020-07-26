package com.nisovin.magicspells.spells;

import java.util.List;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.ConfigData;
import com.nisovin.magicspells.util.MagicConfig;

public class PermissionSpell extends InstantSpell {

	@ConfigData(field="duration", dataType="int", defaultValue="0")
	private int duration;
	
	@ConfigData(field="permission-nodes", dataType="String[]", defaultValue="null")
	private List<String> permissionNodes;
	
	public PermissionSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.duration = getConfigInt("duration", 0);
		this.permissionNodes = getConfigStringList("permission-nodes", null);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL && duration > 0 && this.permissionNodes != null) {
			for (String node : this.permissionNodes) {
				player.addAttachment(MagicSpells.plugin, node, true, this.duration);
			}
			playSpellEffects(EffectPosition.CASTER, player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
