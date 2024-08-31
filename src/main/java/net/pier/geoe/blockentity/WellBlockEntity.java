package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.blockentity.valve.ValveBlockEntity;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;
import net.pier.geoe.gui.WellMenu;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeMultiBlocks;

public abstract class WellBlockEntity extends MultiBlockControllerEntity<TemplateMultiBlock> {



    public final EnergyStorage energyStorage = new EnergyStorage(10000);

    private Reservoir reservoir;

    private final LazyOptional<Reservoir> tankHandler;

    public WellBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, TemplateMultiBlock multiBlock, ValveBlockEntity.Flow flow) {
        super(pType, pPos, pBlockState, multiBlock);
        this.tankHandler = LazyOptional.of(this::getReservoir);

    }

    public Reservoir getReservoir()
    {
        if(this.getLevel() instanceof ServerLevel serverLevel)
        {
            var capabilityOptional = serverLevel.getCapability(ReservoirCapability.CAPABILITY).resolve();
            capabilityOptional.ifPresent(reservoirCapability -> reservoir = reservoirCapability.getReservoir(new ChunkPos(this.getBlockPos())));
        }
        return this.reservoir;
    }



    @Override
    public LazyOptional<Object>[] getHandlers() {
        return new LazyOptional[]{this.tankHandler};
    }

    @Override
    public GeoeContainerMenu<?> getMenu(MenuContext<?> menuContext) {
        return new WellMenu(menuContext.windowId(), menuContext.inv(), this);
    }

    @Override
    public void writeTag(CompoundTag tag) {
        super.writeTag(tag);
    }

    @Override
    public void readTag(CompoundTag tag) {
        super.readTag(tag);
    }

    public static class Production extends WellBlockEntity
    {
        public Production(BlockPos pPos, BlockState pBlockState) {
            super(GeoeBlocks.PRODUCTION_WELL_BE.get(), pPos, pBlockState, GeoeMultiBlocks.PRODUCTION_WELL.get(), ValveBlockEntity.Flow.OUTPUT);
        }
    }

    public static class Injection extends WellBlockEntity
    {
        public Injection(BlockPos pPos, BlockState pBlockState) {
            super(GeoeBlocks.INJECTION_WELL_BE.get(), pPos, pBlockState, GeoeMultiBlocks.INJECTION_WELL.get(), ValveBlockEntity.Flow.INPUT);
        }
    }

}
