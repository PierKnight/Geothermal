package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.WellBlockEntity;

public class InjectionWellBlock extends ControllerBlock<WellBlockEntity>
{
    public InjectionWellBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public WellBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WellBlockEntity.Injection(blockPos,blockState);
    }

}
