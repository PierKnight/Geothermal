package net.pier.geoe.capability.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.pier.geoe.block.EnumPipeConnection;
import oshi.util.tuples.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.channels.Pipe;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class PipeInfo implements INBTSerializable<CompoundTag>
{
    private EnumPipeConnection[] connection = new EnumPipeConnection[6];
    UUID networkUUID;
    int tankConnections = 0;



    public PipeInfo()
    {
        this(new PipeNetwork());
    }

    public PipeNetwork network;

    public PipeInfo(PipeNetwork pipeNetwork)
    {
        Arrays.fill(connection,EnumPipeConnection.NONE);
        this.networkUUID = pipeNetwork.getIdentifier();
        this.network = pipeNetwork;


    }

    public EnumPipeConnection getConnection(Direction direction)
    {
        return connection[direction.get3DDataValue()];
    }

    public void setConnection(Direction direction, EnumPipeConnection connection)
    {
        if(this.connection[direction.get3DDataValue()].isTankConnection()) tankConnections -= 1;
        this.connection[direction.get3DDataValue()] = connection;
        if(connection.isTankConnection()) tankConnections += 1;
    }

    public boolean hasTankConnections()
    {
        return tankConnections > 0;
    }

    @Nullable
    public PipeNetwork getNetwork(Level level)
    {
        Optional<WorldNetworkCapability> lazeCap = level.getCapability(WorldNetworkCapability.CAPABILITY).resolve();
        if(lazeCap.isPresent())
            return lazeCap.get().networks.get(networkUUID);
        return null;
    }


    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("network",this.networkUUID);
        ListTag listTag = new ListTag();
        for(int i = 0;i < 6;i++)
        {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connectionType",(byte) connection[i].ordinal());
            listTag.add(connectionTag);
        }
        tag.put("connections",listTag);
        tag.putByte("tankConnections",(byte) this.tankConnections);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.networkUUID = nbt.getUUID("network");

        ListTag listTag = nbt.getList("connections",10);
        for (int i = 0; i < listTag.size(); i++)
            this.connection[i] = EnumPipeConnection.indexOf(listTag.getCompound(i).getByte("connectionType"));
        this.tankConnections = nbt.getByte("tankConnections");
    }
}
