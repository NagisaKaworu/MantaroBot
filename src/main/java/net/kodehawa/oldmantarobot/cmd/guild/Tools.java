package net.kodehawa.oldmantarobot.cmd.guild;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.kodehawa.mantarobot.modules.Category;
import net.kodehawa.mantarobot.modules.CommandType;
import net.kodehawa.mantarobot.modules.Module;
import net.kodehawa.mantarobot.modules.SimpleCommand;

import java.util.List;

public class Tools extends Module {

	private List<Message> messageHistory;
	private int messagesToPrune;

	public Tools() {
		super(Category.MODERATION);
		this.registerCommands();
	}

	@Override
	public void registerCommands() {
		super.register("ban", new SimpleCommand() {
			@Override
			public CommandType commandType() {
				return CommandType.ADMIN;
			}

			@Override
			public void onCommand(String[] args, String content, GuildMessageReceivedEvent event) {
				//Initialize the variables I'll need.
				Guild guild = event.getGuild();
				User author = event.getAuthor();
				TextChannel channel = event.getChannel();
				Message receivedMessage = event.getMessage();

				//We need to check if this is in a guild AND if the member trying to kick the person has KICK_MEMBERS permission.
				if (guild.getMember(author).hasPermission(Permission.BAN_MEMBERS)) {
					//If you mentioned someone to ban, continue.
					if (receivedMessage.getMentionedUsers().isEmpty()) {
						channel.sendMessage("\u274C" + "You need to mention at least one user to ban.").queue();
						return;
					}

					//For all mentioned members..
					List<User> mentionedUsers = receivedMessage.getMentionedUsers();
					for (User user : mentionedUsers) {
						Member member = guild.getMember(user);
						//If one of them is in a higher hierarchy than the bot, I cannot ban them.
						if (!guild.getSelfMember().canInteract(member)) {
							channel.sendMessage("\u274C" + "Cannot ban member " + member.getEffectiveName() + ", they are higher or the same " + "hierachy than I am!").queue();
							return;
						}

						//If I cannot ban, well..
						if (!guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
							channel.sendMessage("\u274C" + "Sorry! I don't have permission to ban members in this server!").queue();
							return;
						}

						//Proceed to ban them. Again, using queue so I don't get rate limited.
						//Also delete all messages from past 7 days.
						guild.getController().ban(member, 7).queue(
							success -> channel.sendMessage(":zap: You will be missed... or not " + member.getEffectiveName()).queue(),
							error ->
							{
								if (error instanceof PermissionException) {
									PermissionException pe = (PermissionException) error; //Which permission am I missing?

									channel.sendMessage("\u274C" + "Error banning " + member.getEffectiveName()
										+ ": " + "(No permission provided: " + pe.getPermission() + ")").queue();
								} else {
									channel.sendMessage("\u274C" + "Unknown error while banning " + member.getEffectiveName()
										+ ": " + "<" + error.getClass().getSimpleName() + ">: " + error.getMessage()).queue();

									//I need more information in the case of an unexpected error.
									error.printStackTrace();
								}
							});
					}
				} else {
					channel.sendMessage("\u274C " + "Cannot ban. Possible errors: You have no Tools Members permission or this was triggered outside of a guild.").queue();
				}
			}

			@Override
			public String help() {
				return "";
			}

		});
		super.register("kick", new SimpleCommand() {
			@Override
			public String help() {
				return "";
			}

			@Override
			public void onCommand(String[] args, String content, GuildMessageReceivedEvent event) {
				Guild guild = event.getGuild();
				User author = event.getAuthor();
				TextChannel channel = event.getChannel();
				Message receivedMessage = event.getMessage();

				//We need to check if this is in a guild AND if the member trying to kick the person has KICK_MEMBERS permission.
				if (receivedMessage.isFromType(ChannelType.TEXT) && guild.getMember(author).hasPermission(Permission.KICK_MEMBERS)) {
					//If they mentioned a user this gets passed, if they didn't it just doesn't.
					if (receivedMessage.getMentionedUsers().isEmpty()) {
						channel.sendMessage("\u274C" + "You must mention 1 or more users to be kicked!").queue();
					} else {
						Member selfMember = guild.getSelfMember();

						//Do I have permissions to kick members, if yes continue, if no end command.
						if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
							channel.sendMessage("\u274C" + "Sorry! I don't have permission to kick members in this server!").queue();
							return;
						}

						List<User> mentionedUsers = receivedMessage.getMentionedUsers();
						//For all mentioned users in the command.
						for (User user : mentionedUsers) {
							Member member = guild.getMember(user);
							//If one of them is in a higher hierarchy than the bot, cannot kick.
							if (!selfMember.canInteract(member)) {
								channel.sendMessage("\u274C" + "Cannot kick member: " + member.getEffectiveName() + ", they are higher or the same " + "hierachy than I am!").queue();
								return;
							}

							//Proceed to kick them. Again, using queue so I don't get rate limited.
							guild.getController().kick(member).queue(
								success -> channel.sendMessage(":zap: You will be missed... or not " + member.getEffectiveName()).queue(), //Quite funny, I think.
								error ->
								{
									if (error instanceof PermissionException) {
										PermissionException pe = (PermissionException) error; //Which permission?

										channel.sendMessage("\u274C" + "Error kicking [" + member.getEffectiveName()
											+ "]: " + "(No permission provided: " + pe.getPermission() + ")").queue();
									} else {
										channel.sendMessage("\u274C" + "Unknown error while kicking [" + member.getEffectiveName()
											+ "]: " + "<" + error.getClass().getSimpleName() + ">: " + error.getMessage()).queue();

										//Just so I get more info in the case of an unexpected error.
										error.printStackTrace();
									}
								});
						}
					}
				} else {
					channel.sendMessage("\u274C " + "Cannot kick. Possible errors: You have no Kick Members permission or this was triggered outside of a guild.").queue();
				}
			}



			@Override
			public CommandType commandType() {
				return CommandType.ADMIN;
			}
		});
		super.register("prune", new SimpleCommand() {
			@Override
			public String help() {
				return "";
			}

			@Override
			public void onCommand(String[] args, String content, GuildMessageReceivedEvent event) {
				//Initialize normal variables declared in Command so I can use them here.
				Guild guild = event.getGuild();
				User author = event.getAuthor();
				TextChannel channel = event.getChannel();
				Message receivedMessage = event.getMessage();

				//If the received message is from a guild and the person who triggers the command has Manage Messages permissions, continue.
				if (receivedMessage.isFromType(ChannelType.TEXT) && guild.getMember(author).hasPermission(Permission.MESSAGE_MANAGE)) {
					//If you specified how many messages.
					if (!content.isEmpty()) {
						messagesToPrune = Integer.parseInt(content); //Content needs to be a number, you know.
						//I cannot get more than 100 messages from the past, so if the number is more than 100, proceed to default to 100.
						if (messagesToPrune > 100) {
							messagesToPrune = 100;
						}
						TextChannel channel2 = event.getGuild().getTextChannelById(channel.getId());
						//Retrieve the past x messages to delete as a List<Message>
						try {
							messageHistory = channel2.getHistory().retrievePast(messagesToPrune).complete();
						} catch (Exception e) {
							e.printStackTrace();
						}

						//Delete the last x messages. Doing this as a queue so I can avoid rate limiting too, after queuing check if it was successful or no, and if it wasn't warn the user.
						channel2.deleteMessages(messageHistory).queue(
							success -> channel.sendMessage("\uD83D\uDCDD Successfully pruned " + messagesToPrune + " messages").queue(),
							error ->
							{
								if (error instanceof PermissionException) {
									PermissionException pe = (PermissionException) error; //Which permission am I missing?

									channel.sendMessage("\u274C " + "Lack of permission while pruning messages" + "(No permission provided: " + pe.getPermission() + ")").queue();
								} else {
									channel.sendMessage("\u274C " + "Unknown error while pruning messages" + "<" + error.getClass().getSimpleName() + ">: " + error.getMessage()).queue();
									//Just so I get more data in a unexpected scenario.
									error.printStackTrace();
								}
							});
					} else {
						channel.sendMessage("\u274C No messages to prune.").queue();
					}
				} else {
					channel.sendMessage("\u274C " + "Cannot prune. Possible errors: You have no Manage Messages permission or this was triggered outside of a guild.").queue();
				}
			}

			@Override
			public CommandType commandType() {
				return CommandType.ADMIN;
			}
		});
	}
}