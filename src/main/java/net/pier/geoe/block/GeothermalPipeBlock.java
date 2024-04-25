package net.pier.geoe.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.pier.geoe.capability.ReservoirCapability;
import net.pier.geoe.capability.WorldNetworkCapability;
import net.pier.geoe.world.GeyserFeature;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeothermalPipeBlock extends Block
{

    private static final Direction[] DIRECTIONS = Direction.values();
    public static final EnumProperty<EnumPipeConnection> NORTH = EnumProperty.create("north", EnumPipeConnection.class);
    public static final EnumProperty<EnumPipeConnection> SOUTH = EnumProperty.create("south", EnumPipeConnection.class);
    public static final EnumProperty<EnumPipeConnection> WEST = EnumProperty.create("west", EnumPipeConnection.class);
    public static final EnumProperty<EnumPipeConnection> EAST = EnumProperty.create("east", EnumPipeConnection.class);
    public static final EnumProperty<EnumPipeConnection> UP = EnumProperty.create("up", EnumPipeConnection.class);
    public static final EnumProperty<EnumPipeConnection> DOWN = EnumProperty.create("down", EnumPipeConnection.class);

    public static final Map<Direction, EnumProperty<EnumPipeConnection>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (p_55164_) -> {
        p_55164_.put(Direction.NORTH, NORTH);
        p_55164_.put(Direction.EAST, EAST);
        p_55164_.put(Direction.SOUTH, SOUTH);
        p_55164_.put(Direction.WEST, WEST);
        p_55164_.put(Direction.UP, UP);
        p_55164_.put(Direction.DOWN, DOWN);
    }));

    private static final double SWEET_45 = Math.cos(Math.PI / 4D);

    protected final VoxelShape[] shapeByIndex;


    public GeothermalPipeBlock(BlockBehaviour.Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, EnumPipeConnection.NONE).setValue(EAST, EnumPipeConnection.NONE).setValue(SOUTH, EnumPipeConnection.NONE).setValue(WEST, EnumPipeConnection.NONE).setValue(UP, EnumPipeConnection.NONE).setValue(DOWN, EnumPipeConnection.NONE));
        this.shapeByIndex = this.makeShapes(0.3125F);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pOldState, boolean pIsMoving)
    {
        if(pLevel.isClientSide || pIsMoving)
            return;
        if(pOldState.is(this))
            return;

        if(pLevel instanceof ServerLevel serverLevel)
        {
            pLevel.getChunkAt(pPos).getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap ->
            {
                System.out.println(cap.getValue(pPos.getX() >> 4, pPos.getZ() >> 4) + " + " + new ChunkPos(pPos));
            });
        }
        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(capability -> capability.onPipePlaced(pLevel,pPos));
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public BlockState updateShape(@Nonnull BlockState pState,@Nonnull Direction pDirection,@Nonnull BlockState pNeighborState,@Nonnull LevelAccessor pLevel,@Nonnull BlockPos pCurrentPos,@Nonnull BlockPos pNeighborPos)
    {
        if(pLevel instanceof Level)
        {
            LazyOptional<WorldNetworkCapability> lazeCap = ((Level)pLevel).getCapability(WorldNetworkCapability.CAPABILITY);
            lazeCap.ifPresent(capability -> capability.pipeChanged((Level)pLevel,pCurrentPos, pState, pDirection,pNeighborState));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState pState, Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving)
    {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

        if(pIsMoving)
            return;

        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(capability -> capability.onPipeBroken(pLevel, pPos));
    }


    private void useOnFacing(Level pLevel, BlockState pState, BlockPos pPos, Direction direction, boolean sneak)
    {
        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);

        if(!pLevel.isClientSide)
        {
            lazeCap.ifPresent(capability ->
            {
                if(!sneak)
                {
                    EnumProperty<EnumPipeConnection> sideProperty = PROPERTY_BY_DIRECTION.get(direction);

                    BlockState nearBlockState = pLevel.getBlockState(pPos.relative(direction));

                    if(nearBlockState.is(this))
                        capability.updateConnection(pLevel,pPos,direction,!pState.getValue(sideProperty).isConnected());
                    else
                        capability.updateTankConnection(pLevel,pPos,direction);
                }
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public InteractionResult use(@Nonnull BlockState pState,@Nonnull Level pLevel, BlockPos pPos,@Nonnull Player pPlayer,@Nonnull InteractionHand pHand, BlockHitResult pHit)
    {
        if(pPlayer.getItemInHand(pHand).is(Items.EMERALD))
        {
            Vec3 vec = pHit.getLocation().subtract(pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D);

            for (Direction direction : DIRECTIONS)
            {
                double e = vec.normalize().dot(new Vec3(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ()));
                if(e >= SWEET_45)
                {
                    useOnFacing(pLevel, pState, pPos, direction, false);
                    return InteractionResult.SUCCESS;
                }

            }
            useOnFacing(pLevel, pState, pPos, pHit.getDirection(), false);
            return InteractionResult.SUCCESS;
        }
        else
        {
            useOnFacing(pLevel, pState, pPos, pHit.getDirection(), true);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {

    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
    {
        if(pLevel.isClientSide)
            return;

        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(capability -> capability.onPipePlaced(pLevel,pPos));
    }

    private VoxelShape[] makeShapes(float pApothem) {
        float f = 0.5F - pApothem;
        float f1 = 0.5F + pApothem;
        VoxelShape voxelshape = Block.box(f * 16.0F, f * 16.0F, f * 16.0F, f1 * 16.0F, f1 * 16.0F, f1 * 16.0F);
        VoxelShape[] avoxelshape = new VoxelShape[DIRECTIONS.length];

        for(int i = 0; i < DIRECTIONS.length; ++i) {
            Direction direction = DIRECTIONS[i];
            avoxelshape[i] = Shapes.box(0.5D + Math.min(-pApothem, (double)direction.getStepX() * 0.5D), 0.5D + Math.min(-pApothem, (double)direction.getStepY() * 0.5D), 0.5D + Math.min(-pApothem, (double)direction.getStepZ() * 0.5D), 0.5D + Math.max(pApothem, (double)direction.getStepX() * 0.5D), 0.5D + Math.max(pApothem, (double)direction.getStepY() * 0.5D), 0.5D + Math.max(pApothem, (double)direction.getStepZ() * 0.5D));
        }

        VoxelShape[] avoxelshape1 = new VoxelShape[64];
        for(int k = 0; k < 64; ++k) {
            VoxelShape voxelshape1 = voxelshape;

            for(int j = 0; j < DIRECTIONS.length; ++j) {
                if ((k & 1 << j) != 0) {
                    voxelshape1 = Shapes.or(voxelshape1, avoxelshape[j]);
                }
            }
            avoxelshape1[k] = voxelshape1;
        }
        return avoxelshape1;
    }

    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.shapeByIndex[this.getAABBIndex(pState)];
    }

    protected int getAABBIndex(BlockState pState) {
        int i = 0;
        for(int j = 0; j < DIRECTIONS.length; ++j)
            if (pState.getValue(PROPERTY_BY_DIRECTION.get(DIRECTIONS[j])).isConnected())
                i |= 1 << j;
        return i;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }



}
