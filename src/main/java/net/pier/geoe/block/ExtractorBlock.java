package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.ExtractorBlockEntity;

public class ExtractorBlock extends ControllerBlock<ExtractorBlockEntity>
{
    public ExtractorBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public ExtractorBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ExtractorBlockEntity(blockPos,blockState);
    }

}
