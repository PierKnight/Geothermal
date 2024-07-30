package net.pier.geoe.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;

import java.util.Collection;
import java.util.function.Supplier;

public class PacketMultiBlockInfo implements IPacket{

    private final Collection<MultiBlockInfo.StructureData> multiBlockInfos;

    public PacketMultiBlockInfo(Collection<MultiBlockInfo.StructureData> multiBlockInfos) {
        this.multiBlockInfos = multiBlockInfos;
    }


    public PacketMultiBlockInfo(FriendlyByteBuf buf)
    {
        this.multiBlockInfos = buf.readList(friendlyByteBuf -> MultiBlockInfo.StructureData.readFromTag(buf.readNbt()));

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

        buf.writeCollection(this.multiBlockInfos, (friendlyByteBuf, multiBlockInfo) -> friendlyByteBuf.writeNbt(multiBlockInfo.writeToTag(new CompoundTag())));
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            this.multiBlockInfos.forEach(structureData -> {
                MultiBlockInfo.MULTIBLOCK_CACHE.put(structureData.resourceLocation(), structureData);
            });
        });
    }
}
