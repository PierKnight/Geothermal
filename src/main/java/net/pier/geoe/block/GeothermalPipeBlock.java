package net.pier.geoe.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.capability.pipe.PipeNetwork;
import net.pier.geoe.capability.pipe.WorldNetworkCapability;
import net.pier.geoe.network.PacketGasSound;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.register.GeoeTags;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeothermalPipeBlock extends Block implements EntityBlock
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

    private static final float APOTHEM = 0.3125F;

    protected final VoxelShape[] shapeByIndex;




    public GeothermalPipeBlock(BlockBehaviour.Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, EnumPipeConnection.NONE).setValue(EAST, EnumPipeConnection.NONE).setValue(SOUTH, EnumPipeConnection.NONE).setValue(WEST, EnumPipeConnection.NONE).setValue(UP, EnumPipeConnection.NONE).setValue(DOWN, EnumPipeConnection.NONE));
        this.shapeByIndex = this.makeShapes(APOTHEM);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pOldState, boolean pIsMoving)
    {
        if(pLevel.isClientSide || pIsMoving)
            return;
        if(pOldState.is(this))
            return;

        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(capability -> capability.onPipePlaced(pLevel,pPos));
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public BlockState updateShape(@Nonnull BlockState pState,@Nonnull Direction pDirection,@Nonnull BlockState pNeighborState,@Nonnull LevelAccessor pLevel,@Nonnull BlockPos pCurrentPos,@Nonnull BlockPos pNeighborPos)
    {
        if(pLevel instanceof ServerLevel level)
        {
            LazyOptional<WorldNetworkCapability> lazeCap = level.getCapability(WorldNetworkCapability.CAPABILITY);
            lazeCap.ifPresent(capability -> capability.pipeChanged(level,pCurrentPos, pState, pDirection,pNeighborState));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState pState, Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving)
    {

        if (!pState.is(pNewState.getBlock())) {
            LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
            if(pLevel.getBlockEntity(pPos) instanceof PipeBlockEntity pipe) {
                PacketManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> pLevel.getChunkAt(pPos)), new PacketGasSound(pPos));
                super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
                lazeCap.ifPresent(capability -> capability.onPipeBroken(pLevel, pPos, pipe));
            }

        }


    }

    private void useOnFacing(Level pLevel, BlockState pState, BlockPos pPos, Direction direction)
    {
        LazyOptional<WorldNetworkCapability> lazeCap = pLevel.getCapability(WorldNetworkCapability.CAPABILITY);
        EnumProperty<EnumPipeConnection> sideProperty = PROPERTY_BY_DIRECTION.get(direction);
        if(!pLevel.isClientSide)
            lazeCap.ifPresent(capability -> capability.updateConnection(pLevel, pPos,direction,!pState.getValue(sideProperty).isConnected()));
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public InteractionResult use(@Nonnull BlockState pState,@Nonnull Level pLevel, BlockPos pPos,@Nonnull Player pPlayer,@Nonnull InteractionHand pHand, BlockHitResult pHit)
    {
        if(GeoeTags.isWrench(pPlayer.getItemInHand(pHand)))
        {
            Vec3 vec = pHit.getLocation().subtract(pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D);
            for (Direction direction : DIRECTIONS)
            {
                Vec3i normal = direction.getNormal();
                double projection = normal.getX() * vec.x + normal.getY() * vec.y + normal.getZ() * vec.z;
                if(projection > APOTHEM)
                {
                    useOnFacing(pLevel, pState, pPos, direction);
                    return InteractionResult.SUCCESS;
                }
            }
            useOnFacing(pLevel, pState, pPos, pHit.getDirection());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {

        return new PipeBlockEntity(pPos,pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        boolean found = PROPERTY_BY_DIRECTION.values().stream().anyMatch(property -> pState.getValue(property) == EnumPipeConnection.INPUT || pState.getValue(property) == EnumPipeConnection.OUTPUT);
        BlockEntityTicker<PipeBlockEntity> ticker = pLevel.isClientSide ? PipeNetwork::clientTick : PipeNetwork::serverTick;
        return found ? (BlockEntityTicker<T>) ticker : null;
    }
}
