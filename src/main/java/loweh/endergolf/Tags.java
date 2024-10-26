package loweh.endergolf;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Holds the tags registered for the Ender Golf mod.
 */
public class Tags {
    public static final TagKey<Block> SCORECARD_HOLDER = TagKey.of(RegistryKeys.BLOCK, Identifier.of("endergolf", "scorecard_holder"));
}
