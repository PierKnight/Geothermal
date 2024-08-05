package net.pier.geoe.blockentity.valve;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.pier.geoe.block.ValveBlock;
import net.pier.geoe.blockentity.BaseBlockEntity;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.register.GeoeBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ValveBlockEntity extends BaseBlockEntity
{

    private BlockPos controllerPos = null;
    private int index = 0;

    private final Type type;
    private final Flow flow;
    private final Direction direction;

    public ValveBlockEntity(BlockPos pPos, BlockState pBlockState, Type type, Flow flow)
    {
        super(Objects.requireNonNull(GeoeBlocks.VALVES_TYPE.get(type, flow)).get(), pPos, pBlockState);
        this.type = type;
        this.flow = flow;
        this.direction = pBlockState.getValue(ValveBlock.FACING);
    }


    @Override
    public void readTag(CompoundTag tag)
    {
        if(tag.contains("controllerPos"))
            this.controllerPos = NbtUtils.readBlockPos(tag.getCompound("controllerPos"));
        this.index = tag.getInt("index");
    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        if(controllerPos != null)
            tag.put("controllerPos",NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("index", this.index);
    }

    public void updateController(@Nullable BlockPos controllerPos, int index) {

        this.controllerPos = controllerPos;
        this.index = index;

        if(controllerPos == null)
            return;

        var controller = getController();
        if(controller == null)
            throw new IllegalStateException(String.format("Invalid Controller Position %s in valve at %s", controllerPos, getBlockPos()));

        var handlerLazyOptional = controller.getHandlers()[this.index];
        if(handlerLazyOptional.resolve().isPresent()) {
            Flow handlerFlow = handlerLazyOptional.resolve().get().getFlow();
            if(handlerFlow != this.flow)
                throw new IllegalStateException(String.format("Used %s Handler for valve of type %s", handlerFlow, this.flow));
        }
        
    }

    @Nullable
    private MultiBlockControllerEntity<?> getController()
    {
        if(level != null && this.controllerPos != null && level.getBlockEntity(this.controllerPos) instanceof MultiBlockControllerEntity<?> controller)
            return controller;
        return null;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {

        MultiBlockControllerEntity<?> controller = getController();
        if(controller == null)
            return super.getCapability(capability, facing);

        if(this.type.capability == capability && facing == this.direction)
            return controller.getHandlers()[this.index].cast();

        return super.getCapability(capability, facing);
    }

    public enum Type {
        FLUID("fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY),
        ENERGY("energy", CapabilityEnergy.ENERGY);


        public final String name;
        private final Capability<?> capability;
        Type(String name, Capability<?> capability) {
            this.name = name;
            this.capability = capability;
        }
    }

    public enum Flow {
        INPUT("input"),
        OUTPUT("output");

        public final String name;
        Flow(String name) {
            this.name = name;
        }
    }
}
