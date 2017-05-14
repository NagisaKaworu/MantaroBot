package net.kodehawa.mantarobot.commands.game;

import br.com.brjdevs.java.utils.extensions.CollectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.commands.AnimeCmds;
import net.kodehawa.mantarobot.commands.anime.CharacterData;
import net.kodehawa.mantarobot.commands.game.core.GameLobby;
import net.kodehawa.mantarobot.commands.game.core.ImageGame;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperation;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperations;
import net.kodehawa.mantarobot.data.oldentities.Player;
import net.kodehawa.mantarobot.utils.Utils;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;
import net.kodehawa.mantarobot.utils.data.DataManager;
import net.kodehawa.mantarobot.utils.data.GsonDataManager;
import net.kodehawa.mantarobot.utils.data.SimpleFileDataManager;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Game[ImageGuess]")
public class ImageGuess extends ImageGame {
	private static final DataManager<List<String>> NAMES = new SimpleFileDataManager("assets/mantaro/texts/animenames.txt");
	private String authToken = AnimeCmds.authToken;
	private String characterName;
	@Getter
	private int maxAttempts = 10;

	public ImageGuess() {
		super(10);
	}

	@Override
	public void call(GameLobby lobby, HashMap<Member, Player> players) {
		InteractiveOperations.create(lobby.getChannel(), "Game", (int) TimeUnit.MINUTES.toMillis(2), OptionalInt.empty(), new InteractiveOperation() {
			@Override
			public boolean run(GuildMessageReceivedEvent e) {
				return callDefault(e, lobby, players, characterName, getAttempts(), maxAttempts);
			}

			@Override
			public void onExpire() {
				lobby.getChannel().sendMessage(EmoteReference.ERROR + "The time ran out! Correct answer was " + characterName).queue();
				GameLobby.LOBBYS.remove(lobby.getChannel());
			}
		});
	}

	@Override
	public boolean onStart(GameLobby lobby) {
		try {
			characterName = CollectionUtils.random(NAMES.get());
			String url = String.format("https://anilist.co/api/character/search/%1s?access_token=%2s", URLEncoder.encode(characterName, "UTF-8"),
				authToken);
			String json = Utils.wget(url, null);
			CharacterData[] character = GsonDataManager.GSON_PRETTY.fromJson(json, CharacterData[].class);
			String imageUrl = character[0].getImage_url_med();
			sendEmbedImage(lobby.getChannel(), imageUrl, eb -> eb
				.setTitle("Guess the character", null)
				.setFooter("You have 10 attempts and 60 seconds. (Type end to end the game)", null)
			).queue();
			return true;
		} catch (Exception e) {
			lobby.getChannel().sendMessage(EmoteReference.ERROR + "Error while setting up a game.").queue();
			log.warn("Exception while setting up a game", e);
			return false;
		}
	}
}