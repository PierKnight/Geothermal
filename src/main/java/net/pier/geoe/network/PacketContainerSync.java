package net.pier.geoe.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.data.DataContainerType;

import java.util.List;
import java.util.function.Supplier;

public class PacketContainerSync implements IPacket{


    private final List<Pair<Integer, DataContainerType.DataPair<?>>> synced;

    public PacketContainerSync(List<Pair<Integer, DataContainerType.DataPair<?>>> synced)
    {
        this.synced = synced;
    }

    public PacketContainerSync(FriendlyByteBuf buf)
    {
        this(buf.readList(buf1 -> Pair.of(buf1.readVarInt(), DataContainerType.read(buf1))));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeCollection(this.synced, (buf1, integerDataPairPair) -> {
            buf1.writeVarInt(integerDataPairPair.getFirst());
            integerDataPairPair.getSecond().write(buf1);
        });
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            Player player = Minecraft.getInstance().player;
            if(player != null && player.containerMenu instanceof GeoeContainerMenu<?> geoeContainerMenu)
                geoeContainerMenu.receiveSync(this.synced);
        });

    }
}
