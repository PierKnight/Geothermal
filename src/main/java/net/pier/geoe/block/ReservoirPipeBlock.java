package net.pier.geoe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class ReservoirPipeBlock extends Block {


    protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);


    public ReservoirPipeBlock(Properties pProperties) {
        super(pProperties);
    }



    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        ItemStack stack = pPlayer.getItemInHand(pHand);

        if(stack.is(this.asItem()))
        {
            if(tryPlacePipe(pPlayer, pHand, stack, pLevel, pPos))
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }

        return InteractionResult.PASS;
    }


    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    private boolean tryPlacePipe(Player player, InteractionHand interactionHand, ItemStack stack, Level level, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(pos);

        BlockItem blockItem = (BlockItem) this.asItem();


        int totalPlaced = 0;

        while(mutableBlockPos.getY() > level.getMinBuildHeight() && stack.getCount() > 0 && totalPlaced < 3)
        {
            mutableBlockPos.move(Direction.DOWN);
            BlockState state = level.getBlockState(mutableBlockPos);
            //SKIP UNBREAKABLE BLOCK WHICH HAS A FULL FACE UP AND DOWN
            if(level.getBlockState(mutableBlockPos).getBlock().defaultDestroyTime() < 0.0 &&
                    state.isFaceSturdy(level,mutableBlockPos, Direction.UP) &&
                    state.isFaceSturdy(level,mutableBlockPos, Direction.DOWN))
                continue;

            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, interactionHand, stack, new BlockHitResult(Vec3.ZERO, Direction.UP, mutableBlockPos, false));
            InteractionResult interactionResult = blockItem.place(blockPlaceContext);
            if(interactionResult == InteractionResult.CONSUME || interactionResult == InteractionResult.SUCCESS)
                totalPlaced += 1;
        }
        return totalPlaced > 0;
    }
}
