package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.BaseBlockEntity;

public abstract class MultiBlockControllerEntity extends BaseBlockEntity
{

    private boolean isComplete = false;


    public MultiBlockControllerEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);

    }
    protected abstract boolean assemble();

    protected abstract boolean disassemble();

    protected abstract boolean isValid();

    public boolean isComplete() {
        return isComplete;
    }

    public void destroy()
    {
        if (this.isComplete)
        {
            this.disassemble();
            this.isComplete = false;
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, MultiBlockControllerEntity be) {

        if(level.getGameTime() % 10 == 0)
        {
            if(be.isValid()) {
                if (!be.isComplete && be.assemble())
                    be.isComplete = true;
            }
            else if(be.isComplete && be.disassemble())
                be.isComplete = false;

        }
    }

    @Override
    public void readTag(CompoundTag tag)
    {
        this.isComplete = tag.getBoolean("complete");

    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        tag.putBoolean("complete",this.isComplete);
    }


}
