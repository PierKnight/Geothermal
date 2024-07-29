package net.pier.geoe.capability.world;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.client.particle.FluidParticleOption;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PipeNetwork implements INBTSerializable<CompoundTag>
{

    public static final int PIPE_CAPACITY = 1000;
    private UUID identifier = UUID.randomUUID();
    int updatingTimes = 0;

    final HashSet<BlockPos> networkPipesList = new HashSet<>()
    {
        private void update()
        {
            internalTank.setCapacity(this.size() * PIPE_CAPACITY);
        }

        @Override
        public boolean add(BlockPos blockPos)
        {
            boolean add = super.add(blockPos);
            update();
            return add;
        }

        @Override
        public boolean remove(Object o)
        {
            boolean remove = super.remove(o);
            update();
            return remove;
        }
    };

    public int outputs;

    final FluidTank internalTank = new FluidTank(0);

    public int getPipesSize()
    {
        return this.networkPipesList.size();
    }

    public Set<BlockPos> getPipes() {
        return ImmutableSet.copyOf(this.networkPipesList);
    }

    private void updateTank(@Nullable IFluidHandler tank, EnumPipeConnection connection)
    {
        if(tank == null)
        {
            if(connection == EnumPipeConnection.OUTPUT)
                this.internalTank.drain(500 / this.outputs, IFluidHandler.FluidAction.EXECUTE);
            return;
        }

        if(connection == EnumPipeConnection.INPUT)
        {
            FluidStack drained = tank.drain(500, IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = this.internalTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            tank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
        else if(this.outputs > 0)
        {
            FluidStack drained = this.internalTank.drain(500 / this.outputs, IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            this.internalTank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity pipe)
    {
        FluidStack fluidStack = pipe.getFluidStack();
        if (!fluidStack.isEmpty()) {

            GeothermalPipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, connectionFacing) -> {
                boolean emptyShape = level.getBlockState(pos.relative(direction)).getCollisionShape(level, pos) == Shapes.empty();
                if (state.getValue(connectionFacing) == EnumPipeConnection.OUTPUT && emptyShape) {
                    for (int i = 0; i < 13; i++) {
                        Vec3i normal = direction.getNormal();
                        double x = (double) pos.getX() + 0.5D;
                        double y = (double) pos.getY() + 0.5D;
                        double z = (double) pos.getZ() + 0.5D;
                        Vec3i otherAxisDirection = new Vec3i(normal.getZ(), normal.getX(), normal.getY());
                        Vec3i otherOther = normal.cross(otherAxisDirection);
                        double randomX = (level.random.nextDouble() - 0.5) * 0.3D;
                        double randomY = (level.random.nextDouble() - 0.5) * 0.3D;
                        double randomZ = (level.random.nextDouble() - 0.5) * 0.3D;

                        double directionX = normal.getX() + randomX * otherAxisDirection.getX() + randomX * otherOther.getX();
                        double directionY = normal.getY() + randomY * otherAxisDirection.getY() + randomY * otherOther.getY();
                        double directionZ = normal.getZ() + randomZ * otherAxisDirection.getZ() + randomZ * otherOther.getZ();
                        x += directionX * 0.5;
                        y += directionY * 0.5;
                        z += directionZ * 0.5;
                        if(fluidStack.getFluid().getAttributes().isGaseous())
                            level.addParticle(new FluidParticleOption(fluidStack.getFluid()), x, y, z, directionX * 0.3D, directionY * 0.3, directionZ * 0.3);
                        else
                            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, pipe.getFluidStack().getFluid().defaultFluidState().createLegacyBlock()), x, y, z, directionX, directionY, directionZ);

                    }
                }
            });

        }

    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity pipe)
    {
        PipeNetwork pipeNetwork = WorldNetworkCapability.getNetwork(level, pos);
        if(pipeNetwork == null)
            return;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();


        pipe.checkForFluidUpdate(pipeNetwork.internalTank.getFluid());
        GeothermalPipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, connectionFacing) ->
        {
            EnumPipeConnection pipeConnection = state.getValue(connectionFacing);
            if (pipeConnection.isTankConnection()) {
                mutableBlockPos.setWithOffset(pos, direction);
                BlockEntity entity = level.getBlockEntity(mutableBlockPos);
                IFluidHandler fluidHandler = null;
                if (entity != null)
                    fluidHandler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
                pipeNetwork.updateTank(fluidHandler, pipeConnection);
            }
        });

    }


    public UUID getIdentifier()
    {
        return identifier;
    }

    public FluidTank getInternalTank()
    {
        return internalTank;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putUUID("Identifier", this.identifier);

        ListTag listTag = new ListTag();
        this.networkPipesList.forEach(pos -> listTag.add(NbtUtils.writeBlockPos(pos)));
        compoundTag.put("networkPipes", listTag);

        compoundTag.putInt("outputs", this.outputs);
        compoundTag.put("internalTank", this.internalTank.writeToNBT(new CompoundTag()));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.identifier = nbt.getUUID("Identifier");

        ListTag listTag = nbt.getList("networkPipes", 10);
        listTag.iterator().forEachRemaining(tag -> this.networkPipesList.add(NbtUtils.readBlockPos((CompoundTag) tag)));

        this.outputs = nbt.getInt("outputs");
        this.internalTank.readFromNBT(nbt.getCompound("internalTank"));
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;
        if(obj instanceof PipeNetwork network)
            return network.identifier.equals(this.identifier);
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(identifier);
    }

}
