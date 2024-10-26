package loweh.endergolf;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Payload for server to send to client when the scorecard is being used.
 * @param data Currently unused. In need of improvement.
 */
public record UseScorecardPayload(int data) implements CustomPayload {
    public static final CustomPayload.Id<UseScorecardPayload> ID = new CustomPayload.Id<>(NetworkingIds.USE_SCORECARD);
    public static final PacketCodec<RegistryByteBuf, UseScorecardPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, UseScorecardPayload::data, UseScorecardPayload::new);
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
