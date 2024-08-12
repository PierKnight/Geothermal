package net.pier.geoe.capability.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public class WorldNetworkCapability implements INBTSerializable<Tag>
{

    public static final Capability<WorldNetworkCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
    {
        @Override
        public String toString()
        {
            return super.toString();
        }
    });

    private static final Direction[] DIRECTIONS = Direction.values();
    public final Map<UUID,PipeNetwork> networks = new HashMap<>();


    public void connectPipe(Level level, BlockPos pos, Direction direction, HashSet<UUID> blacklist)
    {
        BlockPos nearPos = pos.relative(direction);

        PipeBlockEntity nearPipe = getPipe(level,nearPos);
        if(nearPipe == null)
            return;

        PipeBlockEntity pipe = getPipe(level,pos);
        if(pipe == null)
            return;

        PipeNetwork network = this.networks.get(pipe.getNetworkUUID());
        PipeNetwork nearNetwork = this.networks.get(nearPipe.getNetworkUUID());
        if(nearNetwork == null)
            return;
        boolean fluidCompatible = nearNetwork.internalTank.isEmpty() || network.internalTank.isEmpty() || nearNetwork.internalTank.getFluid().isFluidEqual(network.internalTank.getFluid());

        if(!fluidCompatible)
            return;

        syncDirection(level, pos, pipe, direction, EnumPipeConnection.PIPE);

        if(blacklist.contains(nearPipe.getNetworkUUID()))
            return;

        if(nearNetwork.equals(network))
            return;

        PipeNetwork bigNetwork = network.getPipesSize() > nearNetwork.getPipesSize() ? network : nearNetwork;
        PipeNetwork smallNetwork = bigNetwork == network ? nearNetwork : network;
        blacklist.add(bigNetwork.getIdentifier());
        bigNetwork.outputs += smallNetwork.outputs;

        this.networks.remove(smallNetwork.getIdentifier());

        for (BlockPos pipePos : smallNetwork.networkPipesList)
        {
            PipeBlockEntity smallNetworkPipe = getPipe(level, pipePos);
            bigNetwork.networkPipesList.add(pipePos);
            if(smallNetworkPipe != null)
                smallNetworkPipe.setNetworkUUID(bigNetwork.getIdentifier());
        }

        bigNetwork.internalTank.fill(smallNetwork.internalTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
    }

    private int scanNewNetwork(Level level, BlockPos startPos, PipeNetwork newPipeNetwork, PipeBlockEntity oldPipe,FluidStack fluidStack)
    {

        //we use a linked list since it is more efficient to add and remove elements in it
        LinkedList<PipeBlockEntity> pipesToScan = new LinkedList<>();
        pipesToScan.addFirst(getPipe(level, startPos));

        int totalScan = 1;
        while(!pipesToScan.isEmpty())
        {
            PipeBlockEntity pipe = pipesToScan.removeFirst();
            if(pipe != null)
            {
                pipe.setNetworkUUID(newPipeNetwork.getIdentifier());
                ++totalScan;
                newPipeNetwork.outputs += pipe.getTankConnections();
                newPipeNetwork.networkPipesList.add(pipe.getBlockPos());

                for (Direction direction : DIRECTIONS)
                {
                    BlockPos nearBlockPos = pipe.getBlockPos().relative(direction);

                    PipeBlockEntity nearPipeInfo = this.getPipe(level, nearBlockPos);
                    if(pipe.getConnection(direction) == EnumPipeConnection.PIPE && nearPipeInfo != null && !nearPipeInfo.getNetworkUUID().equals(newPipeNetwork.getIdentifier()) && nearPipeInfo.getConnection(direction.getOpposite()) == EnumPipeConnection.PIPE)
                    {
                        pipesToScan.addFirst(nearPipeInfo);
                    }
                }
            }
        }
        newPipeNetwork.internalTank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        return totalScan;
    }

    private PipeNetwork disconnectPipe(Level level, BlockPos pos, HashSet<UUID> found,FluidStack fluidStack)
    {
        PipeBlockEntity pipe = this.getPipe(level, pos);
        if(pipe != null && !found.contains(pipe.getNetworkUUID()))
        {
            this.networks.remove(pipe.getNetworkUUID());
            PipeNetwork newPipeNetwork = new PipeNetwork();
            newPipeNetwork.updatingTimes = scanNewNetwork(level, pos, newPipeNetwork, pipe,fluidStack);
            this.networks.put(newPipeNetwork.getIdentifier(), newPipeNetwork);
            found.add(newPipeNetwork.getIdentifier());
            return newPipeNetwork;
        }
        return null;
    }

    public void onPipePlaced(Level level, BlockPos pos)
    {

        PipeBlockEntity pipe = getPipe(level,pos);
        if(pipe == null)
            return;

        PipeNetwork newPipeNetwork = new PipeNetwork();
        this.networks.put(newPipeNetwork.getIdentifier(), newPipeNetwork);
        newPipeNetwork.networkPipesList.add(pos);

        pipe.setNetworkUUID(newPipeNetwork.getIdentifier());

        HashSet<UUID> nearNetworks = new HashSet<>();
        for (Direction direction : DIRECTIONS) {
            connectPipe(level, pos, direction, nearNetworks);
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
            if(blockEntity != null && blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).isPresent())
                syncDirection(level, pos, pipe,direction, EnumPipeConnection.INPUT);
        }
    }

    public void onPipeBroken(Level world, BlockPos pos, PipeBlockEntity oldPipeInfo)
    {
        PipeNetwork oldNetwork = this.networks.get(oldPipeInfo.getNetworkUUID());
        if(oldNetwork == null) {
            System.out.println("SHOULD NOT BE POSSIBLE");
            this.networks.remove(oldPipeInfo.getNetworkUUID());
            return;
        }

        if(oldNetwork.updatingTimes >= 5000)
        {
            world.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
            oldNetwork.networkPipesList.remove(pos);
            if(oldNetwork.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.getNetworkUUID());
            return;
        }

        int totalNetworks = 0;
        for (Direction direction : DIRECTIONS)
        {
            PipeBlockEntity nearPipe = getPipe(world, pos.relative(direction));
            if(nearPipe != null && oldNetwork.getIdentifier().equals(nearPipe.getNetworkUUID()))
                ++totalNetworks;
        }

        if(totalNetworks <= 1)
        {
            oldNetwork.networkPipesList.remove(pos);
            if(oldNetwork.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.getNetworkUUID());
            oldNetwork.outputs -= oldPipeInfo.getTankConnections();
        }
        else
        {
            HashSet<UUID> newNetworks = new HashSet<>();
            int amount = oldNetwork.internalTank.getFluidAmount();
            for (Direction direction : DIRECTIONS) {
                PipeNetwork newPipeNetwork  = disconnectPipe(world, pos.relative(direction), newNetworks,new FluidStack(oldNetwork.internalTank.getFluid().getFluid(),amount));
                if(newPipeNetwork != null)
                    amount -= newPipeNetwork.getPipes().size() * PipeNetwork.PIPE_CAPACITY;
            }
            this.networks.remove(oldPipeInfo.getNetworkUUID());
        }

    }

    public void pipeChanged(Level level, BlockPos pos, BlockState state, Direction direction, BlockState nearState)
    {
        PipeBlockEntity pipeInfo = getPipe(level, pos);
        if(pipeInfo == null || level.isClientSide)
            return;
        PipeBlockEntity pipeInfoNear = getPipe(level, pos.relative(direction));
        EnumPipeConnection currentConnection = state.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction));

        if(pipeInfoNear != null)
        {
            EnumPipeConnection otherConnection = level.getBlockState(pos.relative(direction)).getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite()));
            if(otherConnection == EnumPipeConnection.PIPE ){
                PipeNetwork network = this.networks.get(pipeInfo.getNetworkUUID());
                PipeNetwork nearNetwork = this.networks.get(pipeInfoNear.getNetworkUUID());
                if(nearNetwork == null || network == null)
                    return;
                boolean fluidCompatible = nearNetwork.internalTank.isEmpty() || network.internalTank.isEmpty() || nearNetwork.internalTank.getFluid().isFluidEqual(network.internalTank.getFluid());
                if(fluidCompatible)
                    this.syncDirection(level, pos, pipeInfo, direction, EnumPipeConnection.PIPE);
            }
            else if(otherConnection == EnumPipeConnection.NONE)
                this.syncDirection(level, pos, pipeInfo, direction, EnumPipeConnection.NONE);
        }
        else if(currentConnection == EnumPipeConnection.PIPE)
            this.syncDirection(level, pos, pipeInfo, direction, EnumPipeConnection.NONE);


    }

    public void syncDirection(Level level, BlockPos pos, PipeBlockEntity pipeInfo, Direction direction, EnumPipeConnection connection)
    {
        EnumPipeConnection oldConnection = level.getBlockState(pos).getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction));



        level.setBlock(pos, level.getBlockState(pos).setValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction), connection), 3);
        pipeInfo.setConnection(direction, connection);

        PipeNetwork network = this.networks.get(pipeInfo.getNetworkUUID());
        if(network == null)
            return;
        if(oldConnection != EnumPipeConnection.OUTPUT && connection == EnumPipeConnection.OUTPUT)
            network.outputs += 1;
        if(oldConnection == EnumPipeConnection.OUTPUT && connection != EnumPipeConnection.OUTPUT)
            network.outputs -= 1;
    }

    public void updateConnection(Level level, BlockPos pos, Direction direction, boolean connected)
    {
        PipeBlockEntity pipe = getPipe(level, pos);
        PipeBlockEntity nearPipe = getPipe(level, pos.relative(direction));
        if(pipe == null)
            return;
        if(nearPipe == null)
        {
            EnumPipeConnection rotatedConnection = level.getBlockState(pos).getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction)).rotateFluidConnection();
            syncDirection(level, pos, pipe, direction, rotatedConnection);
            return;
        }


        if(connected)
        {
            HashSet<UUID> checknet = new HashSet<>();
            connectPipe(level, pos, direction, checknet);
        }
        else
        {
            PipeNetwork pipeNetwork = this.networks.get(pipe.getNetworkUUID());
            HashSet<UUID> newNetworks = new HashSet<>();
            syncDirection(level, pos, pipe, direction, EnumPipeConnection.NONE);
            FluidStack fluidStack = pipeNetwork.getInternalTank().getFluid();

            PipeNetwork newPipeNetwork = disconnectPipe(level, pos.relative(direction), newNetworks,fluidStack);
            if(newPipeNetwork != null) {
                int remainingFluid = (pipeNetwork.getPipes().size() - newPipeNetwork.getPipes().size()) * PipeNetwork.PIPE_CAPACITY;
                disconnectPipe(level, pos, newNetworks, new FluidStack(fluidStack, remainingFluid));
            }
        }
    }

    public void tick(Level world)
    {
        this.networks.values().forEach(network ->
        {
            network.updatingTimes = 0;
        });
        if(world.getGameTime() % 20 == 0) {
            networks.keySet().forEach(uuid -> System.out.println(uuid));
        }
    }

    @Nullable
    private static PipeBlockEntity getPipe(Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)
            return pipe;
        return null;
    }

    @Nullable
    public static WorldNetworkCapability getWorldNetwork(Level level)
    {
        return level.getCapability(CAPABILITY).resolve().orElse(null);
    }

    @Nullable
    public static PipeNetwork getNetwork(Level level, BlockPos pos)
    {
        PipeBlockEntity pipe = getPipe(level, pos);
        WorldNetworkCapability networkCapability = getWorldNetwork(level);
        return pipe != null && pipe.getNetworkUUID() != null && networkCapability != null ? networkCapability.networks.get(pipe.getNetworkUUID()) : null;
    }


    @Override
    public CompoundTag serializeNBT()
    {


        CompoundTag tag = new CompoundTag();

        ListTag networks = new ListTag();
        this.networks.forEach((pos, network) -> networks.add(network.serializeNBT()));
        tag.put("networks", networks);

        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {

        ListTag networks = ((CompoundTag)nbt).getList("networks", 10);

        for (int i = 0; i < networks.size(); i++)
        {
            CompoundTag networkTag = networks.getCompound(i);
            PipeNetwork network = new PipeNetwork();
            network.deserializeNBT(networkTag);
            this.networks.put(network.getIdentifier(),network);
        }
    }

}
