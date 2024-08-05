package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public abstract class ControllerBlock<T extends MultiBlockControllerEntity> extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ControllerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockMachineFrame.COMPLETE, false));
    }

    @Override
    @NotNull
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        if (pLevel.isClientSide)
            return InteractionResult.SUCCESS;

        if(pPlayer instanceof ServerPlayer serverPlayer && pLevel.getBlockEntity(pPos) instanceof MultiBlockControllerEntity<?> controller)
            return controller.use(serverPlayer);
        return InteractionResult.FAIL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState pState, Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving)
    {
        if(pLevel.getBlockEntity(pPos) instanceof MultiBlockControllerEntity<?> controller)
            controller.destroy();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public abstract T newBlockEntity(BlockPos blockPos, BlockState blockState);

    @Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level pLevel, BlockState pState, BlockEntityType<A> pBlockEntityType) {
        return pLevel.isClientSide ? null : (level, blockPos, blockState, t) -> {
            if(t instanceof MultiBlockControllerEntity<?> controller)
                MultiBlockControllerEntity.tick(level,blockPos,blockState,controller);
        };
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(BlockMachineFrame.COMPLETE);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}
