package com.nisovin.magicspells.util;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

public class MagicConversationPrefix implements ConversationPrefix {

	private String prefix;
	
	public MagicConversationPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String getPrefix(ConversationContext paramConversationContext) {
		return this.prefix;
	}

}
