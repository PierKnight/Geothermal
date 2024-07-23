package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.pier.geoe.blockentity.TestBlockEntity;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import org.jetbrains.annotations.Nullable;

public class TestBlock extends ControllerBlock<TestBlockEntity>
{
    public TestBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public TestBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TestBlockEntity(blockPos,blockState);
    }

}
