package net.pier.geoe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketLoseFluid implements IPacket
{
    //0 When the server updated the client about the network condition
    //1 When the client sends back the packet to notify the server,
    //2 When the server sends the packet with the positions not known by the player
    private int packetType = 0;

    //shared stuff
    private ChunkPos chunkPos = null;
    private UUID uuid = null;

    //packet type 0
    private int networkSize = 0;

    //packet type 1
    private boolean clientResponse = false;

    //packet type 2
    private Set<BlockPos> pipeNetworks = new HashSet<>();

    public PacketLoseFluid(ChunkPos chunkPos,UUID uuid)
    {
        //this.chunkPos = chunkPos;
        //this.uuid = uuid;
    }

    public PacketLoseFluid(FriendlyByteBuf buf)
    {
        System.out.println("STO DECOMPRESSANDO LO SCHIFO");
        /*
        this.packetType = buf.readByte();
        this.chunkPos = buf.readChunkPos();
        this.uuid = buf.readUUID();
        this.networkSize = buf.readInt();
        this.clientResponse = buf.readBoolean();
        for(int i = 0;i < networkSize;i++)
            this.pipeNetworks.add(buf.readBlockPos());

         */
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        System.out.println("STO COMPRESSANDO LO SCHIFO");
        /*
        buf.writeByte(this.packetType);
        buf.writeChunkPos(this.chunkPos);
        buf.writeUUID(this.uuid);
        buf.writeInt(this.networkSize);
        buf.writeBoolean(this.clientResponse);
        this.pipeNetworks.forEach(buf::writeBlockPos);

         */
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> {

            if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            {

                System.out.println("RICEVUTO SUL CLIENT: " + this.packetType);
                /*
                switch(packetType)
                {
                    case 0 -> {

                        PacketManager.INSTANCE.sendToServer(new PacketLoseFluid(null,null));
                    }
                    case 1 -> {}
                }

                 */
            }
            else
            {
                System.out.println("RIMBALZATO DAL CLIENT CON VALORE: " + this.uuid);
            }
        });

    }
}
