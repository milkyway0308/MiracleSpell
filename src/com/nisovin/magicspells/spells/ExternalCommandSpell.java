package com.nisovin.magicspells.spells;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.MessageBlocker;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.ValidTargetList;

public class ExternalCommandSpell extends TargetedSpell implements TargetedEntitySpell {
	
	static MessageBlocker messageBlocker;
	
	private boolean castWithItem;
	private boolean castByCommand;
	private List<String> commandToExecute;
	List<String> commandToExecuteLater;
	private int commandDelay;
	private List<String> commandToBlock;
	List<String> temporaryPermissions;
	boolean temporaryOp;
	private boolean requirePlayerTarget;
	boolean blockChatOutput;
	boolean executeAsTargetInstead;
	boolean executeOnConsoleInstead;
	private String strCantUseCommand;
	private String strNoTarget;
	String strBlockedOutput;
	boolean doVariableReplacement;
	boolean useTargetVariablesInstead;
	
	ConversationFactory convoFac;
	private Prompt convoPrompt;

	public ExternalCommandSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.castWithItem = getConfigBoolean("can-cast-with-item", true);
		this.castByCommand = getConfigBoolean("can-cast-by-command", true);
		this.commandToExecute = getConfigStringList("command-to-execute", null);
		this.commandToExecuteLater = getConfigStringList("command-to-execute-later", null);
		this.commandDelay = getConfigInt("command-delay", 0);
		this.commandToBlock = getConfigStringList("command-to-block", null);
		this.temporaryPermissions = getConfigStringList("temporary-permissions", null);
		this.temporaryOp = getConfigBoolean("temporary-op", false);
		this.requirePlayerTarget = getConfigBoolean("require-player-target", false);
		this.blockChatOutput = getConfigBoolean("block-chat-output", false);
		this.executeAsTargetInstead = getConfigBoolean("execute-as-target-instead", false);
		this.executeOnConsoleInstead = getConfigBoolean("execute-on-console-instead", false);
		this.strCantUseCommand = getConfigString("str-cant-use-command", "&4You don't have permission to do that.");
		this.strNoTarget = getConfigString("str-no-target", "No target found.");
		this.strBlockedOutput = getConfigString("str-blocked-output", "");
		this.doVariableReplacement = getConfigBoolean("do-variable-replacement", false);
		this.useTargetVariablesInstead = getConfigBoolean("use-target-variables-instead", false);
		
		if (this.requirePlayerTarget) this.validTargetList = new ValidTargetList(true, false);
		
		if (this.blockChatOutput) {
			if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
				if (messageBlocker == null) messageBlocker = new MessageBlocker();
			} else {
				this.convoPrompt = new StringPrompt() {
					
					@Override
					public String getPromptText(ConversationContext context) {
						return strBlockedOutput;
					}
					
					@Override
					public Prompt acceptInput(ConversationContext context, String input) {
						return Prompt.END_OF_CONVERSATION;
					}
					
				};
				this.convoFac = new ConversationFactory(MagicSpells.plugin)
					.withModality(true)
					.withFirstPrompt(this.convoPrompt)
					.withTimeout(1);
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// Get target if necessary
			Player target = null;
			if (this.requirePlayerTarget) {
				TargetInfo<Player> targetInfo = getTargetedPlayer(player, power);
				if (targetInfo == null) {
					sendMessage(this.strNoTarget, player, args);
					return PostCastAction.ALREADY_HANDLED;
				}
				target = targetInfo.getTarget();
			}
			process(player, target, args);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void process(CommandSender sender, Player target, String[] args) {
		// Get actual sender
		CommandSender actualSender;
		if (this.executeAsTargetInstead) {
			actualSender = target;
		} else if (this.executeOnConsoleInstead) {
			actualSender = Bukkit.getConsoleSender();
		} else {
			actualSender = sender;
		}
		if (actualSender == null) return;
		
		// Grant permissions and op
		boolean opped = false;
		if (actualSender instanceof Player) {
			if (this.temporaryPermissions != null) {
				for (String perm : this.temporaryPermissions) {
					if (actualSender.hasPermission(perm)) continue;
					actualSender.addAttachment(MagicSpells.plugin, perm.trim(), true, 5);
				}
			}
			if (this.temporaryOp && !actualSender.isOp()) {
				opped = true;
				actualSender.setOp(true);
			}
		}
		
		// Perform commands
		try {
			if (this.commandToExecute != null && !this.commandToExecute.isEmpty()) {

				Conversation convo = null;
				if (sender instanceof Player) {
					if (this.blockChatOutput && messageBlocker != null) {
						messageBlocker.addPlayer((Player)sender);
					} else if (this.convoFac != null) {
						convo = this.convoFac.buildConversation((Player)sender);
						convo.begin();
					}
				}
				
				int delay = 0;
				Player varOwner;
				if (!this.useTargetVariablesInstead) {
					varOwner = sender instanceof Player ? (Player)sender : null;
				} else{
					varOwner = target;
				}
				for (String comm : this.commandToExecute) {
					if (comm != null && !comm.isEmpty()) {
						if (this.doVariableReplacement) {
							comm = MagicSpells.doArgumentAndVariableSubstitution(comm,varOwner, args);
						}
						if (args != null && args.length > 0) {
							for (int i = 0; i < args.length; i++) {
								comm = comm.replace("%" + (i + 1), args[i]);
							}
						}
						if (sender != null) comm = comm.replace("%a", sender.getName());
						if (target != null) comm = comm.replace("%t", target.getName());
						if (comm.startsWith("DELAY ")) {
							String[] split = comm.split(" ");
							delay += Integer.parseInt(split[1]);
						} else if (delay > 0) {
							final CommandSender s = actualSender;
							final String c = comm;
							MagicSpells.scheduleDelayedTask(() -> Bukkit.dispatchCommand(s, c), delay);
						} else {
							Bukkit.dispatchCommand(actualSender, comm);
						}
					}
				}
				if (this.blockChatOutput && messageBlocker != null && sender instanceof Player) {
					messageBlocker.removePlayer((Player)sender);
				} else if (convo != null) {
					convo.abandon();
				}
			}
		} catch (Exception e) {
			// Catch all exceptions to make sure we don't leave someone opped
			e.printStackTrace();
		}
		
		// Deop
		if (opped) actualSender.setOp(false);
		
		// Effects
		if (sender instanceof Player) {
			if (target != null) {
				playSpellEffects((Player)sender, target);
			} else {
				playSpellEffects(EffectPosition.CASTER, (Player)sender);
			}
		} else if (sender instanceof BlockCommandSender) {
			playSpellEffects(EffectPosition.CASTER, ((BlockCommandSender)sender).getBlock().getLocation());
		}
		// Add delayed command
		if (this.commandToExecuteLater != null && !this.commandToExecuteLater.isEmpty() && !this.commandToExecuteLater.get(0).isEmpty()) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new DelayedCommand(sender, target), this.commandDelay);
		}
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (this.requirePlayerTarget && target instanceof Player) {
			process(caster, (Player)target, MagicSpells.NULL_ARGS);
			return true;
		}
		return false;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (this.requirePlayerTarget && target instanceof Player) {
			process(null, (Player)target, MagicSpells.NULL_ARGS);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (!this.requirePlayerTarget) {
			process(sender, null, args);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().isOp()) return;
		if (this.commandToBlock == null) return;
		if (this.commandToBlock.isEmpty()) return;
		String msg = event.getMessage();
		for (String comm : this.commandToBlock) {
			comm = comm.trim();
			if (comm.isEmpty()) continue;
			
			if (msg.startsWith("/" + this.commandToBlock)) {
				event.setCancelled(true);
				sendMessage(this.strCantUseCommand, event.getPlayer(), MagicSpells.NULL_ARGS);
				return;
			}
		}
	}
	
	public boolean requiresPlayerTarget() {
		return this.requirePlayerTarget;
	}

	@Override
	public boolean canCastByCommand() {
		return this.castByCommand;
	}

	@Override
	public boolean canCastWithItem() {
		return this.castWithItem;
	}
	
	@Override
	public void turnOff() {
		if (messageBlocker == null) return;
		messageBlocker.turnOff();
		messageBlocker = null;
	}
	
	private class DelayedCommand implements Runnable {

		private CommandSender sender;
		private Player target;
		
		public DelayedCommand(CommandSender sender, Player target) {
			this.sender = sender;
			this.target = target;
		}
		
		@Override
		public void run() {
			// Get actual sender
			CommandSender actualSender;
			if (executeAsTargetInstead) {
				actualSender = this.target;
			} else if (executeOnConsoleInstead) {
				actualSender = Bukkit.getConsoleSender();
			} else {
				actualSender = this.sender;
			}
			if (actualSender == null) return;
			
			// Grant permissions
			boolean opped = false;
			if (actualSender instanceof Player) {
				if (temporaryPermissions != null) {
					for (String perm : temporaryPermissions) {
						if (actualSender.hasPermission(perm)) continue;
						actualSender.addAttachment(MagicSpells.plugin, perm, true, 5);
					}
				}
				if (temporaryOp && !actualSender.isOp()) {
					opped = true;
					actualSender.setOp(true);
				}
			}
			
			// Run commands
			try {
				Conversation convo = null;
				if (this.sender instanceof Player) {
					if (blockChatOutput && messageBlocker != null) {
						messageBlocker.addPlayer((Player)this.sender);
					} else if (convoFac != null) {
						convo = convoFac.buildConversation((Player)this.sender);
						convo.begin();
					}
				}
				for (String comm : commandToExecuteLater) {
					if (comm == null) continue;
					if (comm.isEmpty()) continue;
					if (this.sender != null) comm = comm.replace("%a", this.sender.getName());
					if (this.target != null) comm = comm.replace("%t", this.target.getName());
					Bukkit.dispatchCommand(actualSender, comm);
				}
				if (blockChatOutput && messageBlocker != null && this.sender instanceof Player) {
					messageBlocker.removePlayer((Player)this.sender);
				} else if (convo != null) {
					convo.abandon();
				}
			} catch (Exception e) {
				// Catch exceptions to make sure we don't leave someone opped
				e.printStackTrace();
			}
			
			// Deop
			if (opped) actualSender.setOp(false);
			
			// Graphical effect
			if (this.sender != null) {
				if (this.sender instanceof Player) {
					playSpellEffects(EffectPosition.DISABLED, (Player)this.sender);
				} else if (this.sender instanceof BlockCommandSender) {
					playSpellEffects(EffectPosition.DISABLED, ((BlockCommandSender)this.sender).getBlock().getLocation());
				}
			}
		}
		
	}

}
