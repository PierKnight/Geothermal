package net.pier.geoe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import net.pier.geoe.Geothermal;
import net.pier.geoe.NetworkInfo;
import oshi.util.tuples.Pair;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketLoseFluid implements IPacket
{
    private final UUID network;
    private final FluidStack fluidStack;

    public PacketLoseFluid(FluidStack fluid, UUID network)
    {
        this.fluidStack = fluid.copy();
        this.network = network;
    }

    public PacketLoseFluid(FriendlyByteBuf buf)
    {
        System.out.println("STO DECOMPRESSANDO LO SCHIFO");
        this.fluidStack = FluidStack.readFromPacket(buf);
        this.network = buf.readUUID();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        this.fluidStack.writeToPacket(buf);
        buf.writeUUID(this.network);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {

            NetworkInfo networkInfo = Geothermal.networks.getOrDefault(this.network, new NetworkInfo(this.fluidStack));
            if(fluidStack.isEmpty())
                Geothermal.networks.remove(this.network);
            else
                Geothermal.networks.put(this.network,networkInfo);
        });

    }
}
