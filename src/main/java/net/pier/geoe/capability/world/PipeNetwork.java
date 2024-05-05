package net.pier.geoe.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.network.PacketLoseFluid2;
import net.pier.geoe.network.PacketManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class PipeNetwork implements INBTSerializable<CompoundTag>
{

    private UUID identifier = UUID.randomUUID();
    int updatingTimes = 0;
    final HashSet<BlockPos> networkPipesList = new HashSet<>()
    {
        private void update()
        {
            internalTank.setCapacity(this.size() * 1000);
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

    public final HashSet<BlockPos> tankConnections = new HashSet<>();
    final FluidTank internalTank = new FluidTank(0);

    public int getPipesSize()
    {
        return this.networkPipesList.size();
    }

    private void forEachTank(Level world, WorldNetworkCapability capability, HashSet<BlockPos> posList)
    {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        int fluidAmount = this.internalTank.getFluidAmount();

        LinkedList<ServerPlayer> players = new LinkedList<>();

        for (BlockPos pos : posList)
        {
            if(world.isLoaded(mutableBlockPos)) {
                PipeInfo pipeInfo = capability.getNetwork(pos);
                if (pipeInfo == null)
                    continue;
                for (Direction direction : Direction.values()) {
                    EnumPipeConnection connection = pipeInfo.getConnection(direction);
                    if (connection.isTankConnection()) {
                        mutableBlockPos.setWithOffset(pos, direction);

                        BlockEntity entity = world.getBlockEntity(mutableBlockPos);
                        if (entity != null)
                            entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent((tank) -> updateTank(world, tank, connection, mutableBlockPos));

                    }
                }
            }
        }
        if(fluidAmount != this.internalTank.getFluidAmount() && (fluidAmount == 0 || this.internalTank.getFluidAmount() == 0))
            dio(world);
    }

    public void syncPipe(Level world, BlockPos pos, FluidStack fluidStack)
    {
        //PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(pos.getX(),pos.getY(),pos.getZ(),20,world.dimension());
        //PacketManager.INSTANCE.send(PacketDistributor.NEAR.with(() -> targetPoint),new PacketLoseFluid2(fluidStack, pos.immutable()));
    }

    public void dio(Level world)
    {
        //this.tankConnections.forEach((blockPos -> syncPipe(world,blockPos, this.internalTank.getFluid())));

    }

    private void updateTank(Level level, IFluidHandler tank, EnumPipeConnection connection, BlockPos pos)
    {
        if(connection == EnumPipeConnection.INPUT)
        {
            FluidStack drained = tank.drain(500, IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = this.internalTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            tank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
        else
        {
            FluidStack drained = this.internalTank.drain(500, IFluidHandler.FluidAction.SIMULATE);
            int filledAmount = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            this.internalTank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public void tick(Level world, WorldNetworkCapability capability)
    {
        this.forEachTank(world, capability, this.tankConnections);
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

        ListTag inputListTag = new ListTag();
        this.tankConnections.forEach(pos -> inputListTag.add(NbtUtils.writeBlockPos(pos)));
        compoundTag.put("inputTanks", inputListTag);


        compoundTag.put("internalTank", this.internalTank.writeToNBT(new CompoundTag()));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.identifier = nbt.getUUID("Identifier");

        ListTag listTag = nbt.getList("networkPipes", 10);
        listTag.iterator().forEachRemaining(tag -> this.networkPipesList.add(NbtUtils.readBlockPos((CompoundTag) tag)));

        ListTag inputListTag = nbt.getList("inputTanks", 10);
        inputListTag.iterator().forEachRemaining(tag -> this.tankConnections.add(NbtUtils.readBlockPos((CompoundTag) tag)));

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
