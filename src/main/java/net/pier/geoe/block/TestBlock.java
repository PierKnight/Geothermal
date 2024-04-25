package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.pier.geoe.blockentity.TestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class TestBlock extends Block implements EntityBlock
{
    public TestBlock(Properties pProperties)
    {
        super(pProperties);
    }

    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }


    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {

        if(pLevel.getBlockEntity(pPos) instanceof TestBlockEntity entity)
        {
            entity.updateStructure();
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new TestBlockEntity(pPos, pState);
    }
}
