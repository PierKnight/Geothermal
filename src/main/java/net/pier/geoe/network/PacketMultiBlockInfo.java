package net.pier.geoe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;

import java.util.Collection;
import java.util.function.Supplier;

public class PacketMultiBlockInfo implements IPacket{

    private final Collection<TemplateMultiBlock.StructureData> multiBlockInfos;

    public PacketMultiBlockInfo(Collection<TemplateMultiBlock.StructureData> multiBlockInfos) {
        this.multiBlockInfos = multiBlockInfos;
    }


    public PacketMultiBlockInfo(FriendlyByteBuf buf)
    {
        this.multiBlockInfos = buf.readList(friendlyByteBuf -> TemplateMultiBlock.StructureData.decode(buf));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.multiBlockInfos, (friendlyByteBuf, multiBlockInfo) -> multiBlockInfo.encode(friendlyByteBuf));
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            this.multiBlockInfos.forEach(structureData -> {
                TemplateMultiBlock.MULTIBLOCK_CACHE.put(structureData.resourceLocation, structureData);
            });
        });
    }
}
