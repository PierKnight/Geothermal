package net.pier.geoe.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeTags;

import javax.annotation.Nonnull;

public class WrenchItem extends Item {
    public WrenchItem() {
        super(new Properties().stacksTo(1).tab(Geothermal.CREATIVE_TAB));
    }

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext pContext) {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if(player instanceof ServerPlayer && player.isCrouching() && blockstate.is(GeoeTags.Blocks.WRENCH_BREAKABLE))
        {
            level.destroyBlock(blockpos, true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
