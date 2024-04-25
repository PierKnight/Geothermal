package net.pier.geoe.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.pier.geoe.block.EnumPipeConnection;

import java.util.Arrays;

public class PipeInfo implements INBTSerializable<CompoundTag>
{
    private EnumPipeConnection[] connection = new EnumPipeConnection[6];
    PipeNetwork network;
    int tankConnections = 0;

    public PipeInfo()
    {
        this(new PipeNetwork());
    }

    public PipeInfo(PipeNetwork pipeNetwork)
    {
        Arrays.fill(connection,EnumPipeConnection.NONE);
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

    public PipeNetwork getNetwork()
    {
        return network;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("network",this.network.serializeNBT());
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
        this.network.deserializeNBT(nbt.getCompound("network"));

        ListTag listTag = nbt.getList("connections",10);
        for (int i = 0; i < listTag.size(); i++)
            this.connection[i] = EnumPipeConnection.indexOf(listTag.getCompound(i).getByte("connectionType"));
        this.tankConnections = nbt.getByte("tankConnections");
    }
}
