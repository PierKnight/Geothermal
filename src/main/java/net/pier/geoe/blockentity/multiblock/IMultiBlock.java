package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public interface IMultiBlock {

    void assemble(Level level, BlockPos pos, Direction direction);

    void disassemble(Level level, BlockPos pos, Direction direction);

    boolean checkStructure(Level level, BlockPos pos, Direction direction);

    BlockPos getPivot(Level level);

    Vec3i getSize(Level level);


    default CompoundTag writeToTag(CompoundTag compoundTag){ return new CompoundTag();}
    default void readFromTag(CompoundTag compoundTag){}


    default BlockPos getOffsetPos(Level level, BlockPos pos, Direction direction)
    {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotationPivot(this.getPivot(level));
        if(direction != null)
            settings.setRotation(getRotation(direction));

        return StructureTemplate.calculateRelativePosition(settings, pos).offset(this.getPivot(level).multiply(-1));
    }

    static Rotation getRotation(Direction direction)
    {
        Direction direc = Direction.NORTH;
        if(direction == direc)
            return Rotation.NONE;
        else if(direction == direc.getClockWise())
            return Rotation.CLOCKWISE_90;
        else if(direction == direc.getCounterClockWise())
            return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.CLOCKWISE_180;
    }

    static boolean compareBlockState(BlockState blockState1, BlockState blockState2)
    {
        if(blockState1 == blockState2)
            return true;

        if(blockState1.is(blockState2.getBlock()))
        {
            Optional<Direction> direction1 = blockState1.getOptionalValue(DirectionalBlock.FACING);
            Optional<Direction> direction2 = blockState2.getOptionalValue(DirectionalBlock.FACING);
            return direction1.isEmpty() || direction2.isEmpty() || direction1.get() == direction2.get();
        }
        return false;
    }
}
