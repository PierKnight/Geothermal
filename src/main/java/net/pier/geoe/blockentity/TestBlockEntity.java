package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends MultiBlockControllerEntity
{


    public TestBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(GeoeBlocks.Test.TEST_BE.get(), pPos, pBlockState);
    }

    @Override
    protected boolean assemble() {

        System.out.println("ASSEMBLE!");
        this.syncInfo();
        return true;
    }

    @Override
    protected boolean disassemble() {
        System.out.println("DISASSEMBLE!");
        this.syncInfo();
        return true;
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
    public void readTag(CompoundTag tag)
    {
        super.readTag(tag);

    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        super.writeTag(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {

        return super.getUpdateTag();
    }

    public MultiBlockInfo getMultiBlock()
    {
        return MultiBlockInfo.getMultiBlockInfo(this.getLevel(), new ResourceLocation("geoe:aboba"));
    }

    @Override
    public AABB getRenderBoundingBox() {

        if(getMultiBlock() == null || level == null || !(this.level.getBlockState(getBlockPos()).getBlock() instanceof ControllerBlock<?>))
            return super.getRenderBoundingBox();

        Vec3i size = getMultiBlock().getSize();
        Direction direction = level.getBlockState(worldPosition).getValue(ControllerBlock.FACING);
        BlockPos min = getMultiBlock().getOffsetPos(BlockPos.ZERO,direction);
        BlockPos max = getMultiBlock().getOffsetPos(new BlockPos(size),direction);
        return new AABB(min,max).move(this.getBlockPos());
    }
}
