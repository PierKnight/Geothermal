package net.pier.geoe.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.pier.geoe.capability.CapabilityInitializer;
import net.pier.geoe.capability.reservoir.ReservoirCapability;

import java.util.Random;

@SuppressWarnings("deprecation")
public class ReservoirPipeBlock extends Block {



    public static final EnumProperty<FluidType> FLUID_TYPE = EnumProperty.create("fluid", FluidType.class);

    protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);


    public ReservoirPipeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FLUID_TYPE, FluidType.EMPTY));

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRandom) {

        if(pLevel.getBlockState(pPos.below()).is(Blocks.BEDROCK))
            Minecraft.getInstance().levelRenderer.destroyBlockProgress(pPos.hashCode(),pPos.below(), 7);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

        FluidType fluidType = pState.getValue(FLUID_TYPE);
        if(fluidType != FluidType.EMPTY && fluidType.fluid instanceof FlowingFluid flowingFluid)
            pLevel.setBlock(pPos, flowingFluid.getFlowing(8,true).createLegacyBlock(), 3);
    }

    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        ItemStack stack = pPlayer.getItemInHand(pHand);

        if(pPlayer.isCrouching())
        {
            if(tryRemovePipe(pLevel, pPlayer, pPos))
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        else if(stack.is(this.asItem()))
        {
            if(tryPlacePipe(pPlayer, pHand, stack, pLevel, pHit))
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }

        return InteractionResult.PASS;
    }


    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        System.out.println("INSIDE");
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }


    private boolean canPassThrough(Level level, BlockPos blockPos, BlockState blockState)
    {
        return level.getBlockState(blockPos).getBlock().defaultDestroyTime() < 0.0 &&
                blockState.isFaceSturdy(level, blockPos, Direction.UP) &&
                blockState.isFaceSturdy(level, blockPos, Direction.DOWN);
    }

    private boolean tryRemovePipe(Level level, Player player, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(pos);

        return true;
    }

    private boolean tryPlacePipe(Player player, InteractionHand interactionHand, ItemStack stack, Level level, BlockHitResult blockHitResult)
    {
        ReservoirCapability reservoirCapability = CapabilityInitializer.getCap(level, ReservoirCapability.CAPABILITY);
        if(reservoirCapability == null)
            return false;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(blockHitResult.getBlockPos());

        BlockItem blockItem = (BlockItem) this.asItem();


        Vec3 hitPos = blockHitResult.getLocation();
        int totalPlaced = 0;

        //THE MAX DEPTH IS GIVEN BY THE MIN WORLD HEIGHT PLUS DUG EARTH
        int maxDepth = level.getMinBuildHeight() - reservoirCapability.getReservoir(new ChunkPos(mutableBlockPos)).getDigInfo(mutableBlockPos).getDepth();

        while(mutableBlockPos.getY() > maxDepth && stack.getCount() > 0 && totalPlaced < 3)
        {
            mutableBlockPos.move(Direction.DOWN);
            hitPos = hitPos.add(0,-1,0);
            BlockState state = level.getBlockState(mutableBlockPos);
            //SKIP UNBREAKABLE BLOCK WHICH HAS A FULL FACE UP AND DOWN
            if(canPassThrough(level, mutableBlockPos, state))
                continue;

            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, interactionHand, stack, new BlockHitResult(hitPos, Direction.UP, mutableBlockPos, false));
            InteractionResult interactionResult = blockItem.place(blockPlaceContext);
            if(interactionResult == InteractionResult.CONSUME || interactionResult == InteractionResult.SUCCESS)
                totalPlaced += 1;
        }
        return totalPlaced > 0;
    }


    @Override
    public FluidState getFluidState(BlockState pState) {
        FluidType fluidType = pState.getValue(FLUID_TYPE);
        return fluidType.fluid instanceof FlowingFluid flowingFluid ? flowingFluid.getSource(false) : fluidType.fluid.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FLUID_TYPE);
    }



    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        FluidType fluidType = pState.getValue(FLUID_TYPE);
        if (fluidType != FluidType.EMPTY)
            pLevel.scheduleTick(pCurrentPos, fluidType.fluid, fluidType.fluid.getTickDelay(pLevel));
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    public enum FluidType implements StringRepresentable {
        EMPTY(Fluids.EMPTY),
        WATER(Fluids.WATER),
        LAVA(Fluids.LAVA);
        private final Fluid fluid;

        FluidType(Fluid fluid) {
            this.fluid = fluid;

        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }
}
