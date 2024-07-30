package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.Geothermal;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlockBlockEntity;
import net.pier.geoe.register.GeoeBlocks;

public class ExtractorBlockEntity extends TemplateMultiBlockBlockEntity {
    public ExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GeoeBlocks.EXTRACTOR_BE.get(), pPos, pBlockState, new ResourceLocation(Geothermal.MODID, "extractor"));
    }

    @Override
    protected boolean assemble() {
        return false;
    }

    @Override
    protected boolean disassemble() {
        return false;
    }
}
