package loweh.endergolf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * The scorecard item used to keep track of scores during a game of Ender Golf. Will also have submitting functionality
 * for scores to a leaderboard hosted on a local database once the scorecard is filled out for a given page.
 */
public class Scorecard extends Item {

    public Scorecard(Settings settings) { super(settings); }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return super.use(world, user, hand);
        // Find the ServerPlayerEntity for the user.
        Collection<ServerPlayerEntity> srvPlayers = PlayerLookup.world((ServerWorld) world);

        boolean foundSrvPlayer = false;

        for (ServerPlayerEntity srvPlayer : srvPlayers) {
            if (srvPlayer.getName().equals(user.getName())) {
                // Send use scorecard payload to tell the client to render the ScorecardScreen.
                ServerPlayNetworking.send(srvPlayer, new UseScorecardPayload(1));
                foundSrvPlayer = true;
                break;
            }
        }

        // Only return success if we found the ServerPlayerEntity and sent the useScorecard packet.
        return foundSrvPlayer ? TypedActionResult.success(user.getStackInHand(hand)) : TypedActionResult.fail(user.getStackInHand(hand));
    }
}
