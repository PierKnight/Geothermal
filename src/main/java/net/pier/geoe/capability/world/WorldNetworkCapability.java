package net.pier.geoe.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
import net.pier.geoe.register.GeoeBlocks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

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
    private final Map<BlockPos, PipeInfo> pipeNetworkMap = new HashMap<>();
    public final HashSet<PipeNetwork> networks = new HashSet<>();


    private void getNetworkChunk(Level level, BlockPos pos)
    {
        //level.getChunkAt(pos).getCapability()
    }

    public boolean connectPipe(Level level, BlockPos pos, Direction direction, HashSet<PipeNetwork> blacklist)
    {
        BlockPos nearPos = pos.relative(direction);

        PipeInfo nearPipe = getNetwork(nearPos);
        if(nearPipe == null)
            return false;

        PipeInfo pipe = getNetwork(pos);
        if(pipe == null)
            return false;
        if(blacklist.contains(nearPipe.network))
            return false;

        boolean oneNetworkIsEmpty = nearPipe.network.internalTank.isEmpty() || pipe.network.internalTank.isEmpty();

        if(oneNetworkIsEmpty || nearPipe.network.internalTank.getFluid().isFluidEqual(pipe.network.internalTank.getFluid()))
        {
            PipeNetwork bigNetwork = pipe.network.getPipesSize() > nearPipe.network.getPipesSize() ? pipe.network : nearPipe.network;
            PipeNetwork smallNetwork = bigNetwork == pipe.network ? nearPipe.network : pipe.network;
            blacklist.add(bigNetwork);

            bigNetwork.tankConnections.addAll(smallNetwork.tankConnections);




            for (BlockPos pipePos : smallNetwork.networkPipesList)
            {
                PipeInfo e = getNetwork(pipePos);
                this.networks.remove(e.network);
                bigNetwork.networkPipesList.add(pipePos);
                e.network = bigNetwork;
                this.pipeNetworkMap.put(pipePos, e);


            }

            smallNetwork.dio(level);

            bigNetwork.internalTank.fill(smallNetwork.internalTank.drain(44444, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            syncDirection(level, pos, pipe, direction, EnumPipeConnection.PIPE);
            return true;
        }
        return false;
    }

    private int scanNewNetwork(BlockPos startPos, PipeInfo pipeNetwork, PipeInfo oldNetwork)
    {

        //we use a linked list since it is more efficient to add and remove elements in it
        LinkedList<BlockPos> pipesToScan = new LinkedList<>();
        pipesToScan.addFirst(startPos);

        int oldSize = oldNetwork.network.getPipesSize();
        FluidStack oldFluidAmount = oldNetwork.network.internalTank.getFluid();

        oldNetwork.network = pipeNetwork.network;
        oldNetwork.network.networkPipesList.add(startPos);
        this.pipeNetworkMap.put(startPos, oldNetwork);
        if(oldNetwork.hasTankConnections())
            pipeNetwork.network.tankConnections.add(startPos);

        int totalScan = 1;
        while(!pipesToScan.isEmpty())
        {
            BlockPos qPos = pipesToScan.removeFirst();
            for (Direction direction : DIRECTIONS)
            {
                BlockPos nearBlockPos = qPos.relative(direction);

                PipeInfo nearPipeInfo = this.getNetwork(nearBlockPos);
                if(nearPipeInfo != null && nearPipeInfo.network != pipeNetwork.network && nearPipeInfo.getConnection(direction.getOpposite()) == EnumPipeConnection.PIPE)
                {
                    nearPipeInfo.network = pipeNetwork.network;
                    pipeNetwork.network.networkPipesList.add(nearBlockPos);
                    pipesToScan.addFirst(nearBlockPos);
                    this.pipeNetworkMap.put(nearBlockPos, nearPipeInfo);
                    ++totalScan;
                    if(nearPipeInfo.hasTankConnections())
                        pipeNetwork.network.tankConnections.add(nearBlockPos);
                }

            }
        }

        //this is to distribute the fluid into separated networks
        if(!oldFluidAmount.isEmpty())
        {
            FluidStack newFluid = oldFluidAmount.copy();
            newFluid.setAmount(Math.round(((float) pipeNetwork.network.getPipesSize() / oldSize) * oldFluidAmount.getAmount()));
            pipeNetwork.network.internalTank.setFluid(newFluid);

        }
        return totalScan;
    }

    public void disconnectPipe(Level level, BlockPos pos, Direction direction, HashSet<PipeNetwork> found)
    {
        BlockPos nearPos = pos.relative(direction);

        PipeInfo pipe = this.getNetwork(pos);
        PipeInfo nearPipe = getNetwork(nearPos);

        if(nearPipe != null && !found.contains(nearPipe.network))
        {
            this.networks.remove(nearPipe.network);
            PipeInfo newPipe = new PipeInfo();
            newPipe.network.updatingTimes = scanNewNetwork(nearPos, newPipe, nearPipe);
            this.networks.add(newPipe.network);
            found.add(newPipe.network);
            newPipe.network.dio(level);
        }

        if(pipe != null && !found.contains(pipe.network))
        {
            this.networks.remove(pipe.network);
            PipeInfo newPipe = new PipeInfo();
            newPipe.network.updatingTimes = scanNewNetwork(pos, newPipe, pipe);
            this.networks.add(newPipe.network);
            found.add(newPipe.network);
            newPipe.network.dio(level);
        }

    }

    public void onPipePlaced(Level level, BlockPos pos)
    {

        PipeInfo pipeInfo = new PipeInfo();
        if(this.pipeNetworkMap.putIfAbsent(pos, pipeInfo) == null)
        {
            pipeInfo.network.networkPipesList.add(pos);
            this.networks.add(pipeInfo.network);
        }
        else
        {
            HashSet<PipeNetwork> nearNetworks = new HashSet<>();
            for (Direction direction : DIRECTIONS)
                connectPipe(level, pos, direction, nearNetworks);
        }

    }

    public void onPipeBroken(Level world, BlockPos pos)
    {
        PipeInfo oldPipeInfo = this.pipeNetworkMap.remove(pos);
        if(oldPipeInfo == null)
            return;


        if(oldPipeInfo.network.updatingTimes >= 5000)
        {
            world.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
            oldPipeInfo.network.networkPipesList.remove(pos);
            if(oldPipeInfo.network.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.network);
            return;
        }

        int totalNetworks = 0;
        for (Direction direction : DIRECTIONS)
            if(this.getNetwork(pos.relative(direction)) != null)
                ++totalNetworks;

        if(totalNetworks <= 1)
        {
            oldPipeInfo.network.networkPipesList.remove(pos);
            if(oldPipeInfo.network.networkPipesList.isEmpty())
                networks.remove(oldPipeInfo.network);
        }
        else
        {
            this.networks.remove(oldPipeInfo.network);
            HashSet<PipeNetwork> newNetworks = new HashSet<>();
            for (Direction direction : DIRECTIONS)
                disconnectPipe(world, pos, direction, newNetworks);
        }
    }

    public void pipeChanged(Level level, BlockPos pos, BlockState state, Direction direction, BlockState nearState)
    {
        PipeInfo pipeInfo = getNetwork(pos);
        if(pipeInfo == null)
            return;

        EnumPipeConnection currentConnection = state.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction));

        if(nearState.is(GeoeBlocks.Test.PIPE.get()))
            this.syncDirection(level, pos, pipeInfo, direction, nearState.getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite())));
        else if(currentConnection == EnumPipeConnection.PIPE)
            this.syncDirection(level, pos, pipeInfo, direction, EnumPipeConnection.NONE);

    }

    public void syncDirection(Level level, BlockPos pos, PipeInfo pipeInfo, Direction direction, EnumPipeConnection connection)
    {
        level.setBlock(pos, level.getBlockState(pos).setValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction), connection), 1 | 2 | 64);
        pipeInfo.setConnection(direction, connection);

        if(pipeInfo.hasTankConnections())
        {
            pipeInfo.network.tankConnections.add(pos);

            pipeInfo.network.syncPipe(level,pos, pipeInfo.network.internalTank.getFluid());
        }
        else
        {
            pipeInfo.network.tankConnections.remove(pos);
            pipeInfo.network.syncPipe(level,pos, FluidStack.EMPTY);
        }

    }

    public void updateConnection(Level level, BlockPos pos, Direction direction, boolean connected)
    {
        PipeInfo pipe = getNetwork(pos);
        PipeInfo nearPipe = getNetwork(pos.relative(direction));
        if(pipe == null || nearPipe == null)
            return;

        if(connected)
        {
            HashSet<PipeNetwork> checknet = new HashSet<>();
            connectPipe(level, pos, direction, checknet);
        }
        else
        {
            HashSet<PipeNetwork> newNetworks = new HashSet<>();
            syncDirection(level, pos, pipe, direction, EnumPipeConnection.NONE);
            disconnectPipe(level, pos, direction, newNetworks);
        }
    }

    public boolean updateTankConnection(Level level, BlockPos pos, Direction direction)
    {
        PipeInfo pipeInfo = getNetwork(pos);
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
        this.networks.forEach(network ->
        {
            network.tick(world, this);
            network.updatingTimes = 0;
        });
    }

    @Nullable
    public PipeInfo getNetwork(BlockPos pos)
    {
        return this.pipeNetworkMap.get(pos);
    }

    @Override
    public CompoundTag serializeNBT()
    {

        CompoundTag tag = new CompoundTag();

        ListTag blockposToNetwork = new ListTag();

        this.pipeNetworkMap.forEach((pos, pipeInfo) ->
        {
            CompoundTag networkTag = new CompoundTag();
            networkTag.put("pos", NbtUtils.writeBlockPos(pos));
            networkTag.put("pipeInfo", pipeInfo.serializeNBT());
            blockposToNetwork.add(networkTag);
        });
        tag.put("posToNetwork", blockposToNetwork);

        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {

        ListTag blockposToNetwork = ((CompoundTag)nbt).getList("posToNetwork", 10);

        for (int i = 0; i < blockposToNetwork.size(); i++)
        {
            CompoundTag tag = blockposToNetwork.getCompound(i);

            PipeInfo pipeInfo = new PipeInfo();
            pipeInfo.deserializeNBT(tag.getCompound("pipeInfo"));
            this.pipeNetworkMap.put(NbtUtils.readBlockPos(tag.getCompound("pos")), pipeInfo);
            this.networks.add(pipeInfo.network);
        }
    }

}
