package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;
import net.pier.geoe.register.GeoeMultiBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class TemplateMultiBlockBlockEntity extends MultiBlockControllerEntity
{

    private final ResourceLocation template;

    public TemplateMultiBlockBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, ResourceLocation template) {
        super(pType, pPos, pBlockState);
        this.template = template;
    }


    @Override
    protected boolean assemble() {
        return false;
    }

    @Override
    protected boolean isValid() {

        if(level == null)
            return false;

        Direction direction = level.getBlockState(worldPosition).getValue(ControllerBlock.FACING);
        MultiBlockInfo info = this.getMultiBlock();
        return info != null && info.checkStructure(this.level,direction,this.getBlockPos());
    }


    @Override
    public void writeTag(CompoundTag tag)
    {
        super.writeTag(tag);
    }


    public MultiBlockInfo getMultiBlock()
    {
        return GeoeMultiBlocks.getMultiBlock(this.template);
    }

    @Override
    public AABB getRenderBoundingBox() {

        if(getMultiBlock() == null || level == null || !(this.level.getBlockState(getBlockPos()).getBlock() instanceof ControllerBlock<?>))
            return super.getRenderBoundingBox();

        Vec3i size = getMultiBlock().getSize(level);
        Direction direction = level.getBlockState(worldPosition).getValue(ControllerBlock.FACING);
        BlockPos min = getMultiBlock().getOffsetPos(level, BlockPos.ZERO,direction);
        BlockPos max = getMultiBlock().getOffsetPos(level, new BlockPos(size),direction);
        return new AABB(min,max).move(this.getBlockPos());
    }
}
