package net.kodehawa.mantarobot.commands;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.MantaroBot;
import net.kodehawa.mantarobot.commands.currency.TextChannelGround;
import net.kodehawa.mantarobot.commands.currency.item.Items;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperations;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.data.oldentities.Player;
import net.kodehawa.mantarobot.modules.Command;
import net.kodehawa.mantarobot.modules.CommandRegistry;
import net.kodehawa.mantarobot.modules.Module;
import net.kodehawa.mantarobot.modules.commands.SimpleCommand;
import net.kodehawa.mantarobot.modules.commands.base.Category;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;

import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Module
public class FunCmds {

	@Command
	public static void coinflip(CommandRegistry cr) {
		cr.register("coinflip", new SimpleCommand(Category.FUN) {
			@Override
			protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
				int times;
				if (args.length == 0 || content.length() == 0) times = 1;
				else {
					try {
						times = Integer.parseInt(args[0]);
						if (times > 1000) {
							event.getChannel().sendMessage(EmoteReference.ERROR + "Whoah there! The limit is 1,000 coinflips").queue();
							return;
						}
					} catch (NumberFormatException nfe) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "You need to specify an Integer for the amount of " +
							"repetitions").queue();
						return;
					}
				}

				final int[] heads = {0};
				final int[] tails = {0};
				doTimes(times, () -> {
					if (new Random().nextBoolean()) heads[0]++;
					else tails[0]++;
				});
				String flips = times == 1 ? "time" : "times";
				event.getChannel().sendMessage(EmoteReference.PENNY + " Your result from **" + times + "** " + flips + " yielded " +
					"**" + heads[0] + "** heads and **" + tails[0] + "** tails").queue();
			}

			@Override
			public MessageEmbed help(GuildMessageReceivedEvent event) {
				return helpEmbed(event, "Coinflip command")
					.setDescription("**Flips a coin with a defined number of repetitions**")
					.addField("Usage", "`~>coinflip <number of times>` - **Flips a coin x number of times**", false)
					.build();
			}
		});
	}

	@Command
	public static void dice(CommandRegistry cr) {
		cr.register("roll", new SimpleCommand(Category.FUN) {
			@Override
			protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
				int roll;
				try {
					roll = Integer.parseInt(args[0]);
				} catch (Exception e) {
					roll = 1;
				}
				if (roll >= 100) roll = 100;
				event.getChannel().sendMessage(EmoteReference.DICE + "You got **" + diceRoll(roll) + "** with a total of **" + roll
					+ "** repetitions.").queue();
				TextChannelGround.of(event).dropItemWithChance(6, 5);
			}

			@Override
			public MessageEmbed help(GuildMessageReceivedEvent event) {
				return helpEmbed(event, "Dice command")
						.setDescription("**Roll a 6-sided dice a specified number of times**")
						.addField("Usage", "`~>roll <number of times>` - **Rolls a dice x number of times**", false)
						.build();
			}
		});
	}

	@Command
	public static void marry(CommandRegistry cr) {
		cr.register("marry", new SimpleCommand(Category.FUN) {
			@Override
			public void call(GuildMessageReceivedEvent event, String content, String[] args) {
				if(args.length == 0){
					onError(event);
					return;
				}

				if (args[0].equals("divorce")) {
					Player user = MantaroData.db().getPlayer(event.getMember());

					if (user.getData().getMarriedWith() == null) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "You aren't married with anyone, why don't you get started?").queue();
						return;
					}

					User user1 = user.getData().getMarriedWith() == null
							? null : MantaroBot.getInstance().getUserById(user.getData().getMarriedWith());

					if(user1 == null){
						user.getData().setMarriedWith(null);
						user.getData().setMarriedSince(0L);
						user.save();
						event.getChannel().sendMessage(EmoteReference.CORRECT + "Now you're single. I guess that's nice?").queue();
						return;
					}

					Player marriedWith = MantaroData.db().getPlayer(user1);

					marriedWith.getData().setMarriedWith(null);
					marriedWith.save();
					marriedWith.getData().setMarriedSince(0L);

					user.getData().setMarriedWith(null);
					user.getData().setMarriedSince(0L);
					user.save();
					event.getChannel().sendMessage(EmoteReference.CORRECT + "Now you're single. I guess that's nice?").queue();
					return;
				}

				if (event.getMessage().getMentionedUsers().isEmpty()) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "Mention the user you want to marry with.").queue();
					return;
				}

				User member = event.getAuthor();
				User user = event.getMessage().getMentionedUsers().get(0);

				if (user.getId().equals(event.getAuthor().getId())) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "You cannot marry with yourself.").queue();
					return;
				}

				if (user.isBot()) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "You cannot marry a bot.").queue();
					return;
				}

				if (MantaroData.db().getPlayer(event.getGuild().getMember(user)).getData().isMarried()) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "That user is married already.").queue();
					return;
				}

				if (MantaroData.db().getPlayer(event.getGuild().getMember(member)).getData().isMarried()) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "You are married already.").queue();
					return;
				}

				if (InteractiveOperations.create(event.getChannel(), "Marriage Proposal", (int) TimeUnit.SECONDS.toMillis(120), OptionalInt.empty(), (e) -> {
					if (!e.getAuthor().getId().equals(user.getId())) return false;

					if (e.getMessage().getContent().equalsIgnoreCase("yes")) {
						Player user1 = MantaroData.db().getPlayer(e.getMember());
						Player marry = MantaroData.db().getPlayer(e.getGuild().getMember(member));
						user1.getData().setMarriedWith(member.getId());
						marry.getData().setMarriedWith(e.getAuthor().getId());
						e.getChannel().sendMessage(EmoteReference.POPPER + e.getMember().getEffectiveName() + " accepted the proposal of " + member.getName() + "!").queue();
						user1.save();
						marry.save();
						return true;
					}

					if (e.getMessage().getContent().equalsIgnoreCase("no")) {
						e.getChannel().sendMessage(EmoteReference.CORRECT + "Denied proposal.").queue();
						return true;
					}

					return false;
				})) {
					TextChannelGround.of(event).dropItemWithChance(Items.LOVE_LETTER, 2);
					event.getChannel().sendMessage(EmoteReference.MEGA + user.getName() + ", respond with **yes** or **no** to the marriage proposal from " + event.getAuthor().getName() + ".").queue();

				}
			}

			@Override
			public MessageEmbed help(GuildMessageReceivedEvent event) {
				return helpEmbed(event, "Marriage command")
						.setDescription("**Basically marries you with a user.**")
						.addField("Usage", "`~>marry <@mention>` - **Propose to someone**", false)
						.addField("Divorcing", "Well, if you don't want to be married anymore you can just do `~>marry divorce`", false)
						.build();
			}
		});
	}

	private static int diceRoll(int repetitions) {
		int num = 0;
		int roll;
		for (int i = 0; i < repetitions; i++) {
			roll = new Random().nextInt(6) + 1;
			num = num + roll;
		}
		return num;
	}
}
