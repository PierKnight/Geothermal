package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.blockentity.BaseBlockEntity;
import net.pier.geoe.blockentity.valve.IInputHandler;
import net.pier.geoe.blockentity.valve.ValveEnergyHandler;
import net.pier.geoe.blockentity.valve.ValveFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class MultiBlockControllerEntity<T extends IMultiBlock> extends BaseBlockEntity
{

    private boolean isComplete = false;

    private final T multiBlock;

    protected final Direction direction;




    public MultiBlockControllerEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, T multiBlock)
    {
        super(pType, pPos, pBlockState);
        this.multiBlock = multiBlock;
        this.direction = pBlockState.getValue(ControllerBlock.FACING);


    }

    public T getMultiBlock() {
        return multiBlock;
    }


    public abstract LazyOptional<IInputHandler>[] getHandlers();


    public boolean isComplete() {
        return isComplete;
    }


    public Direction getDirection()
    {
        return this.direction;
    }

    public void destroy()
    {
        if (this.isComplete)
        {
            this.getMultiBlock().disassemble(getLevel(), getBlockPos(), getDirection());
            this.setComplete(false);
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, MultiBlockControllerEntity<?> be) {

        if(level.getGameTime() % 10 == 0)
        {
            if(be.getMultiBlock().checkStructure(level, blockPos, be.getDirection())) {
                if (!be.isComplete) {
                    be.getMultiBlock().assemble(level, blockPos, be.getDirection());
                    be.setComplete(true);
                }
            }
            else if(be.isComplete) {
                be.setComplete(false);
                be.getMultiBlock().disassemble(level, blockPos, be.getDirection());
            }

        }
    }


    @Override
    public void readTag(CompoundTag tag)
    {
        this.isComplete = tag.getBoolean("complete");
        this.getMultiBlock().readFromTag(tag);
    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        tag.putBoolean("complete",this.isComplete);
        this.multiBlock.writeToTag(tag);
    }

    private void setComplete(boolean complete)
    {
        this.isComplete = complete;
        this.setChanged();
        this.syncInfo();
    }

    @Override
    public AABB getRenderBoundingBox() {

        if(getMultiBlock() == null || level == null || !(this.level.getBlockState(getBlockPos()).getBlock() instanceof ControllerBlock<?>))
            return super.getRenderBoundingBox();

        Vec3i size = getMultiBlock().getSize(level);
        Direction direction = getDirection();
        BlockPos min = getMultiBlock().getOffsetPos(level, BlockPos.ZERO,direction);
        BlockPos max = getMultiBlock().getOffsetPos(level, new BlockPos(size),direction);
        return new AABB(min,max).move(this.getBlockPos());
    }


}
