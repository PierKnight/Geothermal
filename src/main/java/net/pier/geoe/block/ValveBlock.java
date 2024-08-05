package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.pier.geoe.blockentity.valve.ValveBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ValveBlock extends Block implements EntityBlock{

    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    private final ValveBlockEntity.Type type;
    private final ValveBlockEntity.Flow flow;

    public ValveBlock(Properties pProperties, ValveBlockEntity.Type type, ValveBlockEntity.Flow flow) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockMachineFrame.COMPLETE, false));
        this.type = type;
        this.flow = flow;

    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, BlockMachineFrame.COMPLETE);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ValveBlockEntity(pPos, pState, this.type, this.flow);
    }
}
