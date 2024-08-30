package net.pier.geoe.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.capability.reservoir.BreakingBlockReservoir;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.capability.reservoir.ReservoirCapability;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PacketReservoirSync implements IPacket{


    private final ChunkPos chunkPos;
    private final FriendlyByteBuf data;

    private final Type type;

    public PacketReservoirSync(ChunkPos chunkPos, Reservoir reservoir, PacketReservoirSync.Type type) {
        this.chunkPos = chunkPos;
        this.type = type;
        this.data = new FriendlyByteBuf(Unpooled.buffer());
        reservoir.writeUpdate(this.data);


    }

    public PacketReservoirSync(FriendlyByteBuf buf)
    {
        this.chunkPos = buf.readChunkPos();
        this.type = buf.readEnum(PacketReservoirSync.Type.class);
        this.data = new FriendlyByteBuf(buf.readBytes(buf.readVarInt()));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

        buf.writeChunkPos(this.chunkPos);
        buf.writeEnum(this.type);
        buf.writeVarInt(this.data.readableBytes());
        buf.writeBytes(this.data);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            ClientLevel level = Minecraft.getInstance().level;
            if(level != null)
                level.getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> {

                    cap.getReservoir(this.chunkPos).readUpdate(this.data);

                    if(this.type == Type.CHUNK_TRACK)
                        cap.tickingReservoirs.add(this.chunkPos);
                    else if(this.type == Type.CHUNK_UNTRACK)
                        cap.tickingReservoirs.remove(this.chunkPos);
                });
        });
    }

    public enum Type
    {
        CHUNK_TRACK,
        CHUNK_UNTRACK,
        UPDATE
    }
}
