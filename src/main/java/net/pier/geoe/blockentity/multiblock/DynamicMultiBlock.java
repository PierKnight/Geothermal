package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class DynamicMultiBlock implements IMultiBlock{


    private final Vec3i minSize;
    private final Vec3i maxSize;

    private Vec3i size = Vec3i.ZERO;

    public DynamicMultiBlock(Vec3i minSize, Vec3i maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Override
    public void assemble(Level level, BlockPos pos, Direction direction) {

    }

    @Override
    public void disassemble(Level level, BlockPos pos, Direction direction) {

    }

    @Override
    public boolean checkStructure(Level level, BlockPos pos, Direction direction) {
        return false;
    }

    @Override
    public BlockPos getPivot(Level level) {
        return null;
    }

    @Override
    public Vec3i getSize(Level level) {
        return null;
    }

    @Override
    public CompoundTag writeToTag(CompoundTag compoundTag) {
        compoundTag.putInt("sizeX", this.size.getX());
        compoundTag.putInt("sizeY", this.size.getY());
        compoundTag.putInt("sizeZ", this.size.getZ());
        return compoundTag;
    }

    @Override
    public void readFromTag(CompoundTag compoundTag) {
        this.size = new Vec3i(compoundTag.getInt("sizeX"),compoundTag.getInt("sizeY"),compoundTag.getInt("sizeZ"));
    }
}
