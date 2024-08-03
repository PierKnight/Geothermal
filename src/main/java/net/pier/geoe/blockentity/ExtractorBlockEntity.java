package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.blockentity.valve.IInputHandler;
import net.pier.geoe.blockentity.valve.ValveEnergyHandler;
import net.pier.geoe.blockentity.valve.ValveFluidHandler;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeMultiBlocks;
import org.jetbrains.annotations.Nullable;

public class ExtractorBlockEntity extends MultiBlockControllerEntity<TemplateMultiBlock> {
    public ExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GeoeBlocks.EXTRACTOR_BE.get(), pPos, pBlockState, GeoeMultiBlocks.EXTRACTOR.get());
    }


    @Override
    public LazyOptional<IInputHandler>[] getHandlers() {
        return new LazyOptional[0];
    }
}
