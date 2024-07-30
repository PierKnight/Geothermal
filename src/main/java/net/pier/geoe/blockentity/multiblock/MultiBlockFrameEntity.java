package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.BaseBlockEntity;

public abstract class MultiBlockFrameEntity extends BaseBlockEntity
{

    private BlockPos controllerPos = null;

    public MultiBlockFrameEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }


    @Override
    public void readTag(CompoundTag tag)
    {
        if(tag.contains("controllerPos"))
            this.controllerPos = NbtUtils.readBlockPos(tag.getCompound("controllerPos"));
    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        if(controllerPos != null)
            tag.put("controllerPos",NbtUtils.writeBlockPos(controllerPos));
    }

    public void updateController(MultiBlockControllerEntity multiBlockControllerEntity)
    {
        this.controllerPos = multiBlockControllerEntity.getBlockPos();
    }

}
