package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.BaseBlockEntity;

public abstract class MultiBlockControllerEntity extends BaseBlockEntity
{


    public MultiBlockControllerEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }

    void onBlockDeattached(BlockPos brokenPos)
    {

    }

    boolean isPaused()
    {
        return false;
    }




    @Override
    public void readTag(CompoundTag tag)
    {

    }

    @Override
    public void writeTag(CompoundTag tag)
    {

    }
}
