package net.pier.geoe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.Geothermal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketLoseFluid2 implements IPacket
{
    //0 When the server updated the client about the network condition
    //1 When the client sends back the packet to notify the server,
    //2 When the server sends the packet with the positions not known by the player
    private final BlockPos pos;
    private final FluidStack fluidStack;

    public PacketLoseFluid2(FluidStack fluid, BlockPos pos)
    {
        this.fluidStack = fluid.copy();
        this.pos = pos;
    }

    public PacketLoseFluid2(FriendlyByteBuf buf)
    {
        System.out.println("STO DECOMPRESSANDO LO SCHIFO");
        this.fluidStack = FluidStack.readFromPacket(buf);
        this.pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        this.fluidStack.writeToPacket(buf);
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            if(this.fluidStack.isEmpty())
                Geothermal.openedPipes.remove(this.pos);
            else
                Geothermal.openedPipes.put(this.pos, this.fluidStack);
            System.out.println(Geothermal.openedPipes.size());
        });

    }
}
