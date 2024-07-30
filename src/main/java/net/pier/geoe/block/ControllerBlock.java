package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import org.jetbrains.annotations.Nullable;

public abstract class ControllerBlock<T extends MultiBlockControllerEntity> extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ControllerBlock(Properties pProperties) {
        super(pProperties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {

        if(pLevel.getBlockEntity(pPos) instanceof MultiBlockControllerEntity controller)
            controller.destroy();

        super.destroy(pLevel, pPos, pState);
    }

    @Nullable
    @Override
    public abstract T newBlockEntity(BlockPos blockPos, BlockState blockState);

    @Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level pLevel, BlockState pState, BlockEntityType<A> pBlockEntityType) {
        return pLevel.isClientSide ? null : (level, blockPos, blockState, t) -> {
            if(t instanceof MultiBlockControllerEntity controller)
                MultiBlockControllerEntity.tick(level,blockPos,blockState,controller);
        };
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(new Property[]{FACING});
    }
}
