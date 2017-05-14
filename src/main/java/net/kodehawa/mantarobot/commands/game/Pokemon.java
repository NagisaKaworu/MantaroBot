package net.kodehawa.mantarobot.commands.game;

import br.com.brjdevs.java.utils.extensions.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.commands.game.core.GameLobby;
import net.kodehawa.mantarobot.commands.game.core.ImageGame;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperation;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperations;
import net.kodehawa.mantarobot.data.oldentities.Player;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;
import net.kodehawa.mantarobot.utils.data.DataManager;
import net.kodehawa.mantarobot.utils.data.SimpleFileDataManager;

import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Game[PokemonTrivia]")
public class Pokemon extends ImageGame {
	private static final DataManager<List<String>> GUESSES = new SimpleFileDataManager("assets/mantaro/texts/pokemonguess.txt");
	private String expectedAnswer;
	private int maxAttempts = 10;

	public Pokemon() {
		super(10);
	}

	@Override
	public void call(GameLobby lobby, HashMap<Member, Player> players) {
		InteractiveOperations.create(lobby.getChannel(), "Game", (int) TimeUnit.MINUTES.toMillis(2), OptionalInt.empty(), new InteractiveOperation() {
			@Override
			public boolean run(GuildMessageReceivedEvent event) {
				return callDefault(event, lobby, players, expectedAnswer, getAttempts(), maxAttempts);
			}

			@Override
			public void onExpire() {
				lobby.getChannel().sendMessage(EmoteReference.ERROR + "The time ran out! Correct answer was " + expectedAnswer).queue();
				GameLobby.LOBBYS.remove(lobby.getChannel());
			}
		});
	}

	public boolean onStart(GameLobby lobby) {
		try {
			String[] data = CollectionUtils.random(GUESSES.get()).split("`");
			String pokemonImage = data[0];
			expectedAnswer = data[1];
			sendEmbedImage(lobby.getChannel(), pokemonImage, eb -> eb
				.setTitle("Who's that pokemon?", null)
				.setFooter("You have 10 attempts and 120 seconds. (Type end to end the game)", null)
			).queue();
			return true;
		} catch (Exception e) {
			lobby.getChannel().sendMessage(EmoteReference.ERROR + "Error while setting up a game.").queue();
			log.warn("Exception while setting up a game", e);
			return false;
		}
	}
}