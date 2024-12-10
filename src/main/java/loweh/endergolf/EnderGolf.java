package loweh.endergolf;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import loweh.endergolf.database.Score;
import loweh.endergolf.database.ScoreAdmin;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.*;

/**
 * Entry point for the main Ender Golf mod.
 */
public class EnderGolf implements ModInitializer {
	public static final String MOD_ID = "assets/endergolf";

	// Database hostname
	public static final String DB_HOSTNAME = "localhost";
	public static final int DB_PORT = 1277;
	public static final String DB_USERNAME = "mod_user";
	public static final String DB_PASS_FILE_PATH = Paths.get(FabricLoader.getInstance().getGameDir().toString(), "mods", "EnderGolf", "secret.txt").toString();
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Command related members
	private static final int CMD_ERR_COLOR = 0xFF0000;

	/**
	 * Initializes the custom items and blocks, as well as registering the payloads for packets sent to the client and
	 * received from the client.
	 */
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		PayloadTypeRegistry.playS2C().register(UseScorecardPayload.ID, UseScorecardPayload.CODEC);
		Items.initialize();
		Blocks.initialize();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("endergolf").then(literal("admin-list").executes(context -> {
			cmdAdminList(context);
			return 1;
		}))));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("endergolf").then(literal("front-9").then(CommandManager.argument("cmd", StringArgumentType.string()).executes(context -> {
			String arg1 = StringArgumentType.getString(context, "cmd");

			switch (arg1) {
				case "server-latest":
					cmdBestFront9Scores(context);
					break;
				default:
					return 0;
			}

			return 1;
		})))));

		ArrayList<Score> scores = Score.allScores(null, null);

		if (scores != null) {
			for (Score scr : scores) {
				System.out.print("Score found: name=" + nameFromUUID(scr.getUUID()) + ", ");
				System.out.print("isFront=" + scr.getIsFront() + ", ");
				int i = 0;
				for (int score : scr.getScores()) {
					i++;
					System.out.print("Hole " + i + ": " + score + ", ");
				}
				System.out.print("dateTime=" + scr.getDateTime().toString() + "\n");
			}
		}
	}

	private String nameFromUUID(String uuid) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?")).build();
		String name = null;

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				Gson gson = new Gson();
				MojangPlayerResponse mpResponse = gson.fromJson(response.body(), MojangPlayerResponse.class);
				name = mpResponse.name;
			}
		} catch (IOException ioEx) {
			System.out.println("Failed to resolve uuid: " + uuid + " due to IO exception: " + ioEx.getMessage());
		} catch (InterruptedException intEx) {
			System.out.println("Failed to resolve uuid: " + uuid + " due to interrupted exception: " + intEx.getMessage());
		} catch (JsonSyntaxException jsEx) {
			System.out.println("Failed to resolve uuid: " + uuid + " due to JSON exception: " + jsEx.getMessage());
		}

		return name;
	}

	private void cmdAdminList(CommandContext<ServerCommandSource> ctx) {
		ArrayList<ScoreAdmin> admins = ScoreAdmin.allFromDatabase();

		if (admins == null) {
			ctx.getSource().sendFeedback(() -> Text.literal("ERROR: Could not retrieve admin information from database.").withColor(CMD_ERR_COLOR), false);
			return;
		}

		ctx.getSource().sendFeedback(() -> Text.literal("Id    Name    Date Granted"), false);
		ctx.getSource().sendFeedback(() -> Text.literal("__________________________"), false);
		for (ScoreAdmin admin : admins) {
			String name = nameFromUUID(admin.uuid);

			if (name == null) {
				continue;
			}

			Text msg = Text.literal(admin.id + "    " + name + "    " + admin.dtGranted.toString());
			ctx.getSource().sendFeedback(() -> msg, false);
		}
	}

	private void cmdBestFront9Scores(CommandContext<ServerCommandSource> ctx) {
		ArrayList<Score> scores = Score.allScores("WHERE score.id IN (SELECT score_id FROM score_verified WHERE 1=1) AND hole_9_score.is_front = 1 AND (SELECT COUNT(*) FROM hole_9_score WHERE score_id = score.id) = 1 ORDER BY score.date_time LIMIT 10", null);

		if (scores == null) {
			ctx.getSource().sendFeedback(() -> Text.literal("ERROR: Could not retrieve score information from database.").withColor(CMD_ERR_COLOR), false);
			return;
		}

		ctx.getSource().sendFeedback(() -> Text.literal("Name    Total    Date"), false);
		ctx.getSource().sendFeedback(() -> Text.literal("__________________________"), false);
		for (Score score : scores) {
			String name = nameFromUUID(score.getUUID());
			int sum = 0;
			for (int holeScore : score.getScores()) {
				sum += holeScore;
			}

			if (name == null) {
				continue;
			}

			Text msg = Text.literal(name + "    " + sum + "    " + score.getDateTime().toString());
			ctx.getSource().sendFeedback(() -> msg, false);
		}
	}
}