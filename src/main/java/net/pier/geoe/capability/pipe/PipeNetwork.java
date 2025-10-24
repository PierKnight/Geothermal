package net.pier.geoe.capability.pipe;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.client.particle.FluidParticleOption;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PipeNetwork implements INBTSerializable<CompoundTag>
{

    public static final int PIPE_CAPACITY = 1000;
    private UUID identifier = UUID.randomUUID();
    private int size;

    final FluidTank internalTank = new FluidTank(0);

    private Set<PipeOutput> loadedOutputs = new HashSet<>();

    private final WorldNetworkCapability cap;


    public PipeNetwork(WorldNetworkCapability cap)
    {
        this.setSize(1);
        this.cap = cap;
    }


    private void updateTank(Level level, @Nullable IFluidHandler tank, EnumPipeConnection connection)
    {
        if(tank == null)
        {
            if(connection == EnumPipeConnection.OUTPUT)
                this.internalTank.drain(50, IFluidHandler.FluidAction.EXECUTE);
            return;
        }

        if(connection == EnumPipeConnection.INPUT)
        {
            FluidStack drained = tank.drain(this.internalTank.getCapacity(), IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = this.internalTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            tank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
        else if(loadedOutputs.size() > 0)
        {
            int amount = 0;
            FluidStack drained = this.internalTank.drain(amount / loadedOutputs.size(), IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            this.internalTank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity pipe)
    {
        FluidStack fluidStack = pipe.getFluidStack();
        if (!fluidStack.isEmpty()) {

            GeothermalPipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, connectionFacing) -> {
                if (state.getValue(connectionFacing) == EnumPipeConnection.OUTPUT && !GeothermalPipeBlock.IsBlockingLeak(level, pos, direction)) {
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

    public void addOutput(PipeOutput pipeOutput)
    {
        if(this.loadedOutputs.isEmpty())
            this.cap.activeNetworks.put(this.identifier, this);
        this.loadedOutputs.add(pipeOutput);
    }

    public void removeOutput(PipeOutput pipeOutput)
    {
        this.loadedOutputs.remove(pipeOutput);
        if(this.loadedOutputs.isEmpty())
            this.cap.activeNetworks.remove(this.identifier);
    }

    public Set<PipeOutput> getLoadedOutputs() {
        return ImmutableSet.copyOf(this.loadedOutputs);
    }

    public void setSize(int size) {
        this.size = size;
        this.internalTank.setCapacity(this.size * PIPE_CAPACITY);
    }

    public int getSize() {
        return size;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity pipe)
    {
        return;
        /*
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
                pipeNetwork.updateTank(level, fluidHandler, pipeConnection);
            }
        });

         */

    }

    public void tick(Level level)
    {
        if(level.getGameTime() % 20 == 0)
        {
            System.out.println("UPDATING NETWORK " + this.identifier + " Total outputs: " + this.loadedOutputs.size());
        }
    }


    public boolean merge(PipeNetwork other)
    {
        boolean isFluidCompatible = this.internalTank.isEmpty() || other.internalTank.isEmpty() || this.internalTank.getFluid().isFluidEqual(other.internalTank.getFluid());
        if(!isFluidCompatible)
            return false;

        this.setSize(this.getSize() + other.getSize());
        this.internalTank.fill(other.getInternalTank().getFluid(), IFluidHandler.FluidAction.EXECUTE);
        this.loadedOutputs.addAll(other.loadedOutputs);
        if(!this.loadedOutputs.isEmpty())
            this.cap.activeNetworks.put(this.identifier, this);
        return true;
    }

    public void split(PipeNetwork old)
    {
        int fluidAmount = this.getSize() * old.internalTank.getFluidAmount() / old.getSize();

        FluidStack fluidStack = old.internalTank.drain(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
        this.internalTank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
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

        compoundTag.putInt("size", this.size);

        compoundTag.put("internalTank", this.internalTank.writeToNBT(new CompoundTag()));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.identifier = nbt.getUUID("Identifier");
        this.size = nbt.getInt("size");
        this.internalTank.readFromNBT(nbt.getCompound("internalTank"));
    }


    public record PipeOutput(BlockPos pos, Direction direction){
        public static PipeOutput from(BlockPos origin, Direction direction)
        {
            return new PipeOutput(origin.relative(direction), direction);
        }

        public static PipeOutput deserializeNBT(CompoundTag nbt) {
            BlockPos n_pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
            Direction d = Direction.values()[nbt.getInt("direction")];
            return new PipeOutput(n_pos, d);
        }

        public CompoundTag serializeNBT() {
            CompoundTag o = new CompoundTag();
            o.put("pos", NbtUtils.writeBlockPos(this.pos()));
            o.putInt("direction", this.direction().ordinal());
            return o;
        }

    }

}
