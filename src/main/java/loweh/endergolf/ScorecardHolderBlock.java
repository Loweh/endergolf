package loweh.endergolf;

import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.Registry;

/**
 * A new block that onUse will give the player a Scorecard item.
 * In need of a crafting recipe.
 */
public class ScorecardHolderBlock extends Block {
    public ScorecardHolderBlock() {
        super(AbstractBlock.Settings.create().hardness(2).sounds(BlockSoundGroup.WOOD));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        player.getInventory().insertStack(new ItemStack(Registries.ITEM.get(Identifier.of("endergolf", "scorecard"))));
        return ActionResult.SUCCESS;
    }
}
