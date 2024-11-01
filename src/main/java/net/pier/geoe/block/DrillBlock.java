package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class DrillBlock extends Block {

    public static final BooleanProperty MULTIBLOCK = BooleanProperty.create("multiblock");

    private static final VoxelShape SHAPE = box(3,0,3,13,16, 13);


    public DrillBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(MULTIBLOCK, false));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return pState.getValue(MULTIBLOCK) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if(!pState.getValue(MULTIBLOCK))
            return;

        if(!pNewState.is(this) || !pNewState.getValue(MULTIBLOCK)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            mutableBlockPos.set(pPos.relative(Direction.UP));
            dismantleDrill(pLevel, mutableBlockPos, Direction.UP);
            mutableBlockPos.set(pPos.relative(Direction.DOWN));
            dismantleDrill(pLevel, mutableBlockPos, Direction.DOWN);
        }

    }

    private void dismantleDrill(LevelAccessor level, BlockPos.MutableBlockPos pos, Direction direction)
    {
        BlockState state = level.getBlockState(pos);
        if(state.is(this) && state.getValue(MULTIBLOCK)) {
            level.setBlock(pos, state.setValue(MULTIBLOCK, false), 3);
            pos.move(direction);
            this.dismantleDrill(level, pos, direction);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(MULTIBLOCK);
    }
}
