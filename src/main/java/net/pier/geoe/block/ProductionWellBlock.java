package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.WellBlockEntity;

public class ProductionWellBlock extends ControllerBlock<WellBlockEntity>
{
    public ProductionWellBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public WellBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WellBlockEntity.Production(blockPos,blockState);
    }

}
