package loweh.endergolf;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the main Ender Golf mod.
 */
public class EnderGolf implements ModInitializer {
	public static final String MOD_ID = "assets/endergolf";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
	}
}