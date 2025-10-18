package net.pier.geoe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGasSound implements IPacket
{
    private final BlockPos pos;

    public PacketGasSound(BlockPos pos)
    {
        this.pos = pos;
    }

    public PacketGasSound(FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
        });

    }
}
