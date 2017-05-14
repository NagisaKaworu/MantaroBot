package net.kodehawa.mantarobot.data.oldentities.helpers;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Data
public class GuildData {
	private HashMap<String, String> autoroles = new HashMap<>();
	private String birthdayChannel = null;
	private String birthdayRole = null;
	private Long cases = 0L;
	private boolean customAdminLock = false;
	private Set<String> disabledChannels = new HashSet<>();
	private Set<String> disabledCommands = new HashSet<>();
	private String guildAutoRole = null;
	private String guildCustomPrefix = null;
	private String guildLogChannel = null;
	private Set<String> guildUnsafeChannels = new HashSet<>();
	private String joinMessage = null;
	private String leaveMessage = null;
	private Set<String> logExcludedChannels = new HashSet<>();
	private String logJoinLeaveChannel = null;
	private String musicChannel = null;
	private Long musicQueueSizeLimit = null;
	private Long musicSongDurationLimit = null;
	private String mutedRole = null;
	private Long quoteLastId = 0L;
	private boolean rpgDevaluation = true;
	private boolean rpgLocalMode = false;
}
