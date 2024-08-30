package net.pier.geoe.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GeyserWaterBlock extends Block implements BucketPickup {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);

    public GeyserWaterBlock() {
        super(BlockBehaviour.Properties.of(Material.BUBBLE_COLUMN).noCollission().noDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, Boolean.FALSE).setValue(AGE, 0));
    }

    private static boolean canExistIn(BlockState pBlockState) {
        return pBlockState.is(Blocks.BUBBLE_COLUMN) || pBlockState.is(Blocks.WATER) && pBlockState.getFluidState().getAmount() >= 8 && pBlockState.getFluidState().isSource();
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        int age = pState.getValue(AGE);


        if(pState.getValue(ACTIVE)) {
            pLevel.scheduleTick(pPos, this, 7);
            for (Direction direction : Direction.values()) {
                BlockPos relativePos = pPos.relative(direction);
                BlockState state = pLevel.getBlockState(relativePos);
                if (state.is(this) && !state.getValue(ACTIVE)) {
                    pLevel.scheduleTick(relativePos, this, 1);
                }
            }
        }
        else
        {
            updateColumn(pLevel, pPos);
        }
    }

    private void updateColumn(Level level, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(pos.above());
        while(canExistIn(level.getBlockState(mutableBlockPos)))
        {
            level.setBlock(mutableBlockPos,this.defaultBlockState(), 3);
            mutableBlockPos.move(Direction.UP);
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        boolean newState = !pState.getValue(ACTIVE);
        pLevel.setBlock(pPos, pState.setValue(ACTIVE, newState), 3);

        if(newState) {
            for (int i = 0; i < 2; ++i) {
                pLevel.sendParticles(ParticleTypes.CLOUD, (double) pPos.getX() + pLevel.random.nextDouble(), (double) (pPos.getY() + 1), (double) pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
             }
        }
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRandom) {
        double d0 = pPos.getX() + pRandom.nextDouble();
        double d1 = pPos.getY() + pRandom.nextDouble();
        double d2 = pPos.getZ() + pRandom.nextDouble();

        if(pState.getValue(ACTIVE)) {
            //pLevel.addAlwaysVisibleParticle(ParticleTypes.CLOUD, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
            //pLevel.addAlwaysVisibleParticle(ParticleTypes.CLOUD, d0 + (double) pRandom.nextFloat(), d1 + (double) pRandom.nextFloat(), d2 + (double) pRandom.nextFloat(), 0.0D, 0.04D, 0.0D);
        }
        else
        {
            pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE, d0, d1, d2, 0.0D, 0.04D, 0.0D);
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ACTIVE);
        pBuilder.add(AGE);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }
}
