package net.pier.geoe.blockentity.valve;

import com.mojang.datafixers.types.Func;
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
import java.util.function.BiFunction;
import java.util.function.Function;

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

    public void updateController(BlockPos controllerPos, int index) {

        this.controllerPos = controllerPos;
        this.index = index;

        //IInputHandler e = (IInputHandler) this.type.inputs.apply(getController())[index].cast();

        //if(e.isInput() != (flow == Flow.INPUT))
        //    throw new IllegalStateException("INVALID HANDLER FOR THIS VALVE");
        
    }

    @Nullable
    private MultiBlockControllerEntity<?> getController()
    {
        if(level != null && level.getBlockEntity(this.getBlockPos()) instanceof MultiBlockControllerEntity<?> controller)
            return controller;
        return null;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {

        MultiBlockControllerEntity<?> controller = getController();
        if(controller == null)
            return super.getCapability(capability, facing);

        //if(this.type.capability == capability && facing == this.direction.getOpposite())
        //    return this.flow == Flow.INPUT ? this.type.inputs.apply(controller)[index].cast() : this.type.outputs.apply(controller)[index].cast();
        return super.getCapability(capability, facing);
    }

    public enum Type {
        FLUID("fluid"),
        ENERGY("energy");



        public final String name;
        Type(String name) {
            this.name = name;
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
