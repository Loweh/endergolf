package loweh.endergolf;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

public class EnderGolfClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		ClientPlayNetworking.registerGlobalReceiver(UseScorecardPayload.ID, (payload, ctx) -> {
			ctx.client().execute(this::useScorecard);
		});
	}

	public void useScorecard() {
		System.out.println("Creating screen from scorecard.");
		ScorecardScreen scoreScr = new ScorecardScreen();
		MinecraftClient.getInstance().setScreen(scoreScr);
		System.out.println(MinecraftClient.getInstance().player.getUuidAsString());
	}
}