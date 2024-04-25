package net.pier.geoe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacket
{
    void toBytes(FriendlyByteBuf buf);

    void process(Supplier<NetworkEvent.Context> context);
}
