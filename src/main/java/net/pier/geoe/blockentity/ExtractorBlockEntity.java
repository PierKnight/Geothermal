package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.blockentity.valve.IValveHandler;
import net.pier.geoe.blockentity.valve.ValveBlockEntity;
import net.pier.geoe.blockentity.valve.ValveFluidHandler;
import net.pier.geoe.gui.ExtractorMenu;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeMultiBlocks;

public class ExtractorBlockEntity extends MultiBlockControllerEntity<TemplateMultiBlock> {



    public final EnergyStorage energyStorage = new EnergyStorage(10000);
    public final FluidTank tank = new FluidTank(3330000);


    private final LazyOptional<ValveFluidHandler> inputTankHandler = LazyOptional.of(() -> new ValveFluidHandler(this.tank, ValveBlockEntity.Flow.INPUT));
    private final LazyOptional<ValveFluidHandler> outputTankHandler = LazyOptional.of(() -> new ValveFluidHandler(this.tank, ValveBlockEntity.Flow.OUTPUT));

    public ExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GeoeBlocks.EXTRACTOR_BE.get(), pPos, pBlockState, GeoeMultiBlocks.EXTRACTOR.get());


    }


    @Override
    public LazyOptional<IValveHandler>[] getHandlers() {
        return new LazyOptional[]{inputTankHandler, outputTankHandler};
    }

    @Override
    public GeoeContainerMenu<?> getMenu(MenuContext<?> menuContext) {
        return new ExtractorMenu(menuContext.windowId(), menuContext.inv(), this);
    }


    @Override
    public void writeTag(CompoundTag tag) {
        super.writeTag(tag);
        tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void readTag(CompoundTag tag) {
        super.readTag(tag);
        this.tank.readFromNBT(tag.getCompound("tank"));
    }
}
