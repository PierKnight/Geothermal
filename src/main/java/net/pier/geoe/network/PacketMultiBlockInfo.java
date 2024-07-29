package net.pier.geoe.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;
import org.apache.commons.compress.utils.ByteUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketMultiBlockInfo implements IPacket{

    private final Collection<MultiBlockInfo> multiBlockInfos;

    public PacketMultiBlockInfo() {
        this.multiBlockInfos = MultiBlockInfo.getMultiblocks().values();
    }

    public PacketMultiBlockInfo(MultiBlockInfo info) {
        this.multiBlockInfos = List.of(info);
    }

    public PacketMultiBlockInfo(FriendlyByteBuf buf)
    {
        this.multiBlockInfos = buf.readList(friendlyByteBuf -> new MultiBlockInfo(friendlyByteBuf.readNbt()));

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

        buf.writeCollection(this.multiBlockInfos, (friendlyByteBuf, multiBlockInfo) -> friendlyByteBuf.writeNbt(multiBlockInfo.writeToTag(new CompoundTag())));
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            MultiBlockInfo.updateMultiBlocks(this.multiBlockInfos);
        });
    }
}
