package net.pier.geoe.capability.world;

import com.mojang.datafixers.types.Func;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.register.GeoeBlocks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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


    public boolean connectPipe(Level level, BlockPos pos, Direction direction, HashSet<UUID> blacklist)
    {
        BlockPos nearPos = pos.relative(direction);

        PipeBlockEntity nearPipe = getPipe(level,nearPos);
        if(nearPipe == null)
            return false;

        PipeBlockEntity pipe = getPipe(level,pos);
        if(pipe == null)
            return false;
        if(blacklist.contains(nearPipe.getNetworkUUID()))
            return false;

        PipeNetwork network = this.networks.get(pipe.getNetworkUUID());
        PipeNetwork nearNetwork = this.networks.get(nearPipe.getNetworkUUID());

        boolean oneNetworkIsEmpty = nearNetwork.internalTank.isEmpty() || network.internalTank.isEmpty();

        if(oneNetworkIsEmpty || nearNetwork.internalTank.getFluid().isFluidEqual(network.internalTank.getFluid()))
        {
            PipeNetwork bigNetwork = network.getPipesSize() > nearNetwork.getPipesSize() ? network : nearNetwork;
            PipeNetwork smallNetwork = bigNetwork == network ? nearNetwork : network;
            blacklist.add(bigNetwork.getIdentifier());

            bigNetwork.tankConnections.addAll(smallNetwork.tankConnections);

            this.networks.remove(smallNetwork.getIdentifier());

            for (BlockPos pipePos : smallNetwork.networkPipesList)
            {
                PipeBlockEntity e = getPipe(level, pipePos);
                bigNetwork.networkPipesList.add(pipePos);
                e.networkUUID = bigNetwork.getIdentifier();
                //this.putPipe(level, pipePos, e);


                e.syncInfo();


            }

            bigNetwork.internalTank.fill(smallNetwork.internalTank.drain(44444, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            bigNetwork.dio(level);
            syncDirection(level, pos, pipe, direction, EnumPipeConnection.PIPE);

            return true;
        }
        return false;
    }

    private int scanNewNetwork(Level level, BlockPos startPos, PipeNetwork newPipeNetwork, PipeBlockEntity oldPipe,FluidStack fluidStack)
    {

        //we use a linked list since it is more efficient to add and remove elements in it
        LinkedList<BlockPos> pipesToScan = new LinkedList<>();
        pipesToScan.addFirst(startPos);


        newPipeNetwork.networkPipesList.add(startPos);

        oldPipe.networkUUID = newPipeNetwork.getIdentifier();
        oldPipe.syncInfo();

        if(oldPipe.hasTankConnections())
            newPipeNetwork.tankConnections.add(startPos);

        int totalScan = 1;
        while(!pipesToScan.isEmpty())
        {
            BlockPos qPos = pipesToScan.removeFirst();
            for (Direction direction : DIRECTIONS)
            {
                BlockPos nearBlockPos = qPos.relative(direction);

                PipeBlockEntity nearPipeInfo = this.getPipe(level, nearBlockPos);
                if(nearPipeInfo != null && !nearPipeInfo.networkUUID.equals(newPipeNetwork.getIdentifier()) && nearPipeInfo.getConnection(direction.getOpposite()) == EnumPipeConnection.PIPE)
                {
                    nearPipeInfo.networkUUID = newPipeNetwork.getIdentifier();

                    newPipeNetwork.networkPipesList.add(nearBlockPos);
                    pipesToScan.addFirst(nearBlockPos);
                    ++totalScan;
                    if(nearPipeInfo.hasTankConnections())
                        newPipeNetwork.tankConnections.add(nearBlockPos);

                    nearPipeInfo.syncInfo();
                }

            }
        }

        newPipeNetwork.internalTank.setFluid(fluidStack);

        return totalScan;
    }

    public void disconnectPipe(Level level, BlockPos pos, Direction direction, HashSet<UUID> found,FluidStack fluidStack)
    {
        BlockPos nearPos = pos.relative(direction);

        PipeBlockEntity pipe = this.getPipe(level, pos);
        PipeBlockEntity nearPipe = this.getPipe(level, nearPos);




        /*
        if(nearPipe != null && !found.contains(nearPipe.networkUUID))
        {
            System.out.println("DISCONNECT" + nearPos);
            this.networks.remove(nearPipe.networkUUID);
            PipeNetwork newPipeNetwork = new PipeNetwork();
            this.networks.put(newPipeNetwork.getIdentifier(),newPipeNetwork);
            newPipeNetwork.updatingTimes = scanNewNetwork(level, nearPos, newPipeNetwork, nearPipe,fluidStack);
            found.add(newPipeNetwork.getIdentifier());
        }

         */


        if(pipe != null && !found.contains(pipe.networkUUID))
        {
            this.networks.remove(pipe.networkUUID);
            PipeNetwork newPipeNetwork = new PipeNetwork();
            newPipeNetwork.updatingTimes = scanNewNetwork(level, pos, newPipeNetwork, pipe,fluidStack);
            this.networks.put(newPipeNetwork.getIdentifier(), newPipeNetwork);
            found.add(newPipeNetwork.getIdentifier());
        }

    }

    public void onPipePlaced(Level level, BlockPos pos)
    {



        PipeBlockEntity pipe = getPipe(level,pos);
        if(pipe == null)
            return;


        PipeNetwork newPipeNetwork = new PipeNetwork();
        this.networks.put(newPipeNetwork.getIdentifier(), newPipeNetwork);
        newPipeNetwork.networkPipesList.add(pos);
        pipe.networkUUID = newPipeNetwork.getIdentifier();

        HashSet<UUID> nearNetworks = new HashSet<>();
        for (Direction direction : DIRECTIONS)
            connectPipe(level, pos, direction, nearNetworks);

    }

    public void onPipeBroken(Level world, BlockPos pos, Consumer<Boolean> destroy)
    {
        PipeBlockEntity oldPipeInfo = getPipe(world,pos);
        if(oldPipeInfo == null)
            return;
        PipeNetwork oldNetwork = this.networks.get(oldPipeInfo.getNetworkUUID());

        destroy.accept(true);

        if(oldNetwork.updatingTimes >= 5000)
        {
            world.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
            oldNetwork.networkPipesList.remove(pos);
            if(oldNetwork.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.networkUUID);
            return;
        }

        int totalNetworks = 0;
        for (Direction direction : DIRECTIONS)
            if(this.getPipe(world, pos.relative(direction)) != null)
                ++totalNetworks;

        if(totalNetworks <= 1)
        {
            oldNetwork.networkPipesList.remove(pos);
            if(oldNetwork.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.networkUUID);
        }
        else
        {

            HashSet<UUID> newNetworks = new HashSet<>();
            int amount = oldNetwork.internalTank.getFluidAmount();
            for (Direction direction : DIRECTIONS) {
                amount = amount / 2;
                disconnectPipe(world, pos, direction, newNetworks,new FluidStack(oldNetwork.internalTank.getFluid().getFluid(),amount));
            }
            this.networks.remove(oldPipeInfo.networkUUID);
        }
    }

    public void pipeChanged(Level level, BlockPos pos, BlockState state, Direction direction, BlockState nearState)
    {
        PipeBlockEntity pipeInfo = getPipe(level, pos);
        if(pipeInfo == null || level.isClientSide)
            return;

        EnumPipeConnection currentConnection = state.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction));

        if(nearState.is(GeoeBlocks.Test.PIPE.get()))
            this.syncDirection(level, pos, pipeInfo, direction, nearState.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite())));
        else if(currentConnection == EnumPipeConnection.PIPE)
            this.syncDirection(level, pos, pipeInfo, direction, EnumPipeConnection.NONE);

    }

    public void syncDirection(Level level, BlockPos pos, PipeBlockEntity pipeInfo, Direction direction, EnumPipeConnection connection)
    {
        level.setBlock(pos, level.getBlockState(pos).setValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction), connection), 1 | 2 | 64);
        pipeInfo.setConnection(direction, connection);

        PipeNetwork network = this.networks.get(pipeInfo.getNetworkUUID());
        if(network == null)
            return;

        if(pipeInfo.hasTankConnections())
        {
            network.tankConnections.add(pos);
        }
        else
        {
            network.tankConnections.remove(pos);
        }

    }

    public void updateConnection(Level level, BlockPos pos, Direction direction, boolean connected)
    {
        PipeBlockEntity pipe = getPipe(level, pos);
        PipeBlockEntity nearPipe = getPipe(level, pos.relative(direction));
        if(pipe == null || nearPipe == null)
            return;

        if(connected)
        {
            HashSet<UUID> checknet = new HashSet<>();
            connectPipe(level, pos, direction, checknet);
        }
        else
        {
            HashSet<UUID> newNetworks = new HashSet<>();
            syncDirection(level, pos, pipe, direction, EnumPipeConnection.NONE);
            disconnectPipe(level, pos.relative(direction), direction, newNetworks,FluidStack.EMPTY);
            disconnectPipe(level, pos, direction, newNetworks,FluidStack.EMPTY);
        }
    }

    public boolean updateTankConnection(Level level, BlockPos pos, Direction direction)
    {
        PipeBlockEntity pipeInfo = getPipe(level, pos);
        if(pipeInfo == null)
            return false;
        BlockPos nearPos = pos.relative(direction);
        BlockState blockState = level.getBlockState(pos);


        EnumPipeConnection rotatedConnection = blockState.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction)).rotateFluidConnection();
        syncDirection(level, pos, pipeInfo, direction, rotatedConnection);

        return true;
    }

    public void tick(Level world)
    {
        this.networks.values().forEach(network ->
        {
            network.tick(world, this);
            network.updatingTimes = 0;

        });
        if(world.getGameTime() % 20 == 0)
         System.out.println(networks.size());
    }

    @Nullable
    public PipeBlockEntity getPipe(Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)
            return pipe;
        return null;
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
