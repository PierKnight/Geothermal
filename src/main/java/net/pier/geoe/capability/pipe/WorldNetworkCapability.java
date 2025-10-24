package net.pier.geoe.capability.pipe;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.util.SerializerUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class WorldNetworkCapability implements INBTSerializable<Tag>
{

    public static final Capability<WorldNetworkCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
    {
        @Override
        public String toString()
        {return super.toString();
        }
    });

    private final Level level;

    public final Map<UUID, PipeNetwork> activeNetworks = new HashMap<>();

    public WorldNetworkCapability(Level level) {
        this.level = level;
    }


    public void tick(Level world)
    {

        if(level.getGameTime() % 20 == 0 && !world.isClientSide)
        {
            System.out.println("Networks:" + this.pipeNetworks.size());
            System.out.println("Parent:" + this.pipeParent.size());
        }

        activeNetworks.values().forEach(pipeNetwork -> pipeNetwork.tick(this.level));
    }

    @Nullable
    public static WorldNetworkCapability getWorldNetwork(Level level)
    {
        return level.getCapability(CAPABILITY).resolve().orElse(null);
    }

    @Nullable
    public static PipeNetwork getNetwork(Level level, BlockPos pos)
    {
        WorldNetworkCapability networkCapability = getWorldNetwork(level);
        return networkCapability != null ? networkCapability.find(pos).getSecond() : null;
    }

    private Map<BlockPos, BlockPos> pipeParent = new HashMap<>();

    public Map<BlockPos, PipeNetwork> pipeNetworks = new HashMap<>();

    public Pair<BlockPos, PipeNetwork> find(BlockPos pos) {

        BlockPos p = pipeParent.get(pos);
        PipeNetwork network = pipeNetworks.get(pos);
        Pair<BlockPos, PipeNetwork> result = Pair.of(pos, network);

        //if (!p.equals(pos) && pipeNetworks.get(p) == null)
        if (p != null && network == null) {
            result = find(p);
            pipeParent.put(pos, result.getFirst());
        }
        return result;
    }

    public void place(BlockPos pos)
    {
        if(find(pos).getSecond() != null)
            return;

        this.pipeNetworks.put(pos, new PipeNetwork(this));
    }

    public boolean change(BlockPos pos, BlockPos nearPos, BlockState state, BlockState nearState, Direction direction)
    {
        if(GeothermalPipeBlock.areConnected(level, pos, direction))
            return this.union(pos, nearPos);
        return false;
    }


    public boolean union(BlockPos a, BlockPos b) {
        Pair<BlockPos, PipeNetwork> rootA = find(a);
        Pair<BlockPos, PipeNetwork> rootB = find(b);

        //same network skip union
        if (rootA.getFirst().equals(rootB.getFirst())) return false;

        if(rootB.getSecond() != null) {
            if (rootA.getSecond().getSize() < rootB.getSecond().getSize()) {
                Pair<BlockPos, PipeNetwork> tmp = rootA;
                rootA = rootB;
                rootB = tmp;
            }
            if (!rootA.getSecond().merge(rootB.getSecond())) return false;
        }

        //this.pipeNetworks.remove(rootB.getFirst());
        this.removeNetwork(rootB.getFirst());
        pipeParent.put(rootB.getFirst(), rootA.getFirst());

        return true;
    }

    private Set<BlockPos> floodFill(Level level, BlockPos startPos) {
        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        result.add(startPos);
        //reset parenting since we are joining the network again
        pipeParent.remove(startPos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (result.contains(neighbor)) continue;

                if (GeothermalPipeBlock.areConnected(level, pos, dir)) {
                    result.add(neighbor);
                    queue.add(neighbor);
                    pipeParent.remove(neighbor);
                    this.union(pos, neighbor);
                }
            }
        }

        return result;
    }


    public void rebuildNetworks(BlockPos pos, @Nullable Direction direction) {
        Set<BlockPos> checked = new HashSet<>();

        Set<BlockPos> positions = direction != null ? Sets.newHashSet(pos, pos.relative(direction)) : Arrays.stream(Direction.values()).map(pos::relative).collect(Collectors.toSet());

        PipeNetwork removedNetwork = removeNetwork(find(pos).getFirst());//this.pipeNetworks.remove(find(pos).getFirst());
        this.pipeParent.remove(pos);

        for (BlockPos p : positions) {

            if (checked.contains(p)) continue;

            if(!level.getBlockState(p).is(GeoeBlocks.PIPE.get())) continue;

            PipeNetwork newNetwork = new PipeNetwork(this);

            this.pipeNetworks.put(p, newNetwork);

            // Run a flood-fill starting from this neighbor
            Set<BlockPos> connected = floodFill(level, p);

            // Avoid reprocessing the same cluster
            checked.addAll(connected);

            newNetwork.split(removedNetwork);

        }
        //give loaded outputs to corresponding new network
        for (PipeNetwork.PipeOutput output : removedNetwork.getLoadedOutputs()) {
            PipeNetwork otherNetwork = this.find(output.pos().relative(output.direction().getOpposite())).getSecond();
            if(otherNetwork != null)
                otherNetwork.addOutput(output);
        }
    }

    private PipeNetwork removeNetwork(BlockPos pos)
    {
        PipeNetwork network = this.pipeNetworks.remove(pos);
        if(network != null)
            this.activeNetworks.remove(network.getIdentifier());
        return network;
    }

    @Override
    public CompoundTag serializeNBT()
    {

        CompoundTag tag = new CompoundTag();

        ListTag networks = SerializerUtils.mapToNBT(this.pipeNetworks, NbtUtils::writeBlockPos, PipeNetwork::serializeNBT);
        ListTag parent = SerializerUtils.mapToNBT(this.pipeParent, NbtUtils::writeBlockPos, NbtUtils::writeBlockPos);
        tag.put("networks", networks);
        tag.put("parent", parent);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        CompoundTag tag = (CompoundTag)nbt;

        this.pipeNetworks = SerializerUtils.nbtToMap(tag.getList("networks", 10), NbtUtils::readBlockPos, t -> {
            PipeNetwork pipeNetwork = new PipeNetwork(this);
            pipeNetwork.deserializeNBT(t);
            return pipeNetwork;
        });

        this.pipeParent = SerializerUtils.nbtToMap(tag.getList("parent", 10), NbtUtils::readBlockPos, NbtUtils::readBlockPos);



    }

}
