package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.capability.pipe.PipeNetwork;
import net.pier.geoe.capability.pipe.WorldNetworkCapability;
import net.pier.geoe.client.sound.SoundManager;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.NotNull;

public class PipeBlockEntity extends BaseBlockEntity {

    //client side only fluid used to update particles
    private FluidStack fluidStack = FluidStack.EMPTY;


    public PipeBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(GeoeBlocks.PIPE_BE.get(), pPos, pBlockState);
    }


    @Override
    public void setRemoved() {
        super.setRemoved();
        System.out.println("REMOVED" + level.isClientSide);

        if(!level.isClientSide) {
            PipeNetwork network = WorldNetworkCapability.getNetwork(level, this.getBlockPos());
            if(network != null) {
                for (Direction value : Direction.values())
                    network.removeOutput(PipeNetwork.PipeOutput.from(this.getBlockPos(), value));
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        System.out.println("ADDED" + level.isClientSide);

        if(!level.isClientSide) {
            PipeNetwork network = WorldNetworkCapability.getNetwork(level, this.getBlockPos());
            if(network != null) {
                for (Direction value : Direction.values())
                    if(GeothermalPipeBlock.getConnection(this.getBlockState(),value) == EnumPipeConnection.OUTPUT)
                        network.addOutput(PipeNetwork.PipeOutput.from(this.getBlockPos(), value));
            }
        }

    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    @Override
    public void readTag(CompoundTag tag) {


    }

    @Override
    public void writeTag(CompoundTag tag) {

    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {

        CompoundTag tag = new CompoundTag();
        this.fluidStack.writeToNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.fluidStack = FluidStack.loadFluidStackFromNBT(tag);


        GeothermalPipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, enumPipeConnectionEnumProperty) -> {
            //if (!fluidStack.getFluid().getAttributes().isGaseous())
            //    SoundManager.stopGasLeak(getBlockPos(), direction);
            if(fluidStack.getFluid().getAttributes().isGaseous() && this.getBlockState().getValue(enumPipeConnectionEnumProperty) == EnumPipeConnection.OUTPUT)
                SoundManager.playGasLeak(this, direction);
                //SoundManager.playGasLeak(getBlockPos(), direction);
        });
    }

    public void updateFluidStack(FluidStack fluidStack)
    {
        this.fluidStack = fluidStack;
    }



}
