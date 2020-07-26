package com.nisovin.magicspells.spells.targeted;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.ConfigReaderUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.prompt.ConversationContextUtil;

public class ConversationSpell extends TargetedSpell {

	private ConversationFactory conversationFactory;
	
	public ConversationSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		conversationFactory = ConfigReaderUtil.readConversationFactory(getConfigSection("conversation"));
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state,
			float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
			if (targetInfo == null || targetInfo.getTarget() == null) return noTarget(player);
			
			Player target = targetInfo.getTarget();
			Conversation c = conversationFactory.buildConversation(target);
			ConversationContextUtil.setconversable(c.getContext(), target);
			c.begin();
			return PostCastAction.HANDLE_NORMALLY;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
