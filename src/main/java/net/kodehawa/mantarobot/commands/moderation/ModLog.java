package net.kodehawa.mantarobot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.kodehawa.mantarobot.MantaroBot;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.data.oldentities.DBGuild;

public class ModLog {

	public enum ModAction {
		TEMP_BAN, BAN, KICK, MUTE, UNMUTE
	}

	public static void log(Member author, User target, String reason, ModAction action, long caseN, String... time) {
		DBGuild guildDB = MantaroData.db().getGuild(author.getGuild());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Responsible Moderator", author.getEffectiveName(), true);
		embedBuilder.addField("Member", target.getName(), true);
		embedBuilder.addField("Reason", reason, false);
		embedBuilder.setThumbnail(target.getEffectiveAvatarUrl());
		switch (action) {
			case BAN:
				embedBuilder.setAuthor("Ban | Case #" + caseN, null, author.getUser().getEffectiveAvatarUrl());
				break;
			case TEMP_BAN:
				embedBuilder.setAuthor("Temp Ban | Case #" + caseN, null, author.getUser().getEffectiveAvatarUrl());
				embedBuilder.addField("Time", time[0], true);
				break;
			case KICK:
				embedBuilder.setAuthor("Kick | Case #" + caseN, null, author.getUser().getEffectiveAvatarUrl());
				break;
			case MUTE:
				embedBuilder.setAuthor("Mute | Case #" + caseN, null, author.getUser().getEffectiveAvatarUrl());
				break;
			case UNMUTE:
				embedBuilder.setAuthor("Un-mute | Case #" + caseN, null, author.getUser().getEffectiveAvatarUrl());
				break;
		}
		if (guildDB.getData().getGuildLogChannel() != null) {
			MantaroBot.getInstance().getTextChannelById(guildDB.getData().getGuildLogChannel()).sendMessage(embedBuilder.build()).queue();
		}
	}

	public static void logUnban(Member author, String target, String reason) {
		DBGuild guildDB = MantaroData.db().getGuild(author.getGuild());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Responsible Moderator", author.getEffectiveName(), true);
		embedBuilder.addField("Member ID", target, true);
		embedBuilder.addField("Reason", reason, false);
		embedBuilder.setAuthor("Unban", null, author.getUser().getEffectiveAvatarUrl());
		if (guildDB.getData().getGuildLogChannel() != null) {
			MantaroBot.getInstance().getTextChannelById(guildDB.getData().getGuildLogChannel()).sendMessage(embedBuilder.build()).queue();
		}
	}
}

