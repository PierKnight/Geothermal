package net.pier.geoe.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.capability.reservoir.BreakingBlockReservoir;

import java.util.List;
import java.util.function.Supplier;

public class PacketBreakingBlocks implements IPacket{


    private final List<BreakingBlockReservoir> breakingBlocks;

    public PacketBreakingBlocks(List<BreakingBlockReservoir> breakingBlocks) {
        this.breakingBlocks = breakingBlocks;
    }

    public PacketBreakingBlocks(FriendlyByteBuf buf)
    {
        breakingBlocks = buf.readList(buf1 -> new BreakingBlockReservoir(buf1.readBlockPos(), buf1.readVarInt(),0));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(breakingBlocks, (buf1, integerBlockPosPair) -> {
            buf1.writeBlockPos(integerBlockPosPair.pos);
            buf1.writeVarInt(integerBlockPosPair.progress);
        });
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {

            for (BreakingBlockReservoir breakingBlock : this.breakingBlocks) {
                Minecraft.getInstance().levelRenderer.destroyBlockProgress(breakingBlock.pos.hashCode(),breakingBlock.pos, breakingBlock.progress);
            }
        });
    }
}
