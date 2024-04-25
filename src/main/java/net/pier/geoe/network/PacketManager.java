package net.pier.geoe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.pier.geoe.Geothermal;

import java.util.Optional;
import java.util.function.Function;

public class PacketManager
{
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Geothermal.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int id = 0;

    public static void init()
    {
        registerMessage(PacketLoseFluid.class, PacketLoseFluid::new);
        registerMessage(PacketLoseFluid2.class, PacketLoseFluid2::new);
    }




    private static <MSG extends IPacket> void registerMessage(Class<MSG> msgClass, Function<FriendlyByteBuf, MSG> decoder)
    {
        registerMessage(msgClass, decoder, Optional.empty());
    }
    private static <MSG extends IPacket> void registerMessage(Class<MSG> msgClass, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection direction)
    {
        registerMessage(msgClass, decoder, Optional.of(direction));
    }

    private static <MSG extends IPacket> void registerMessage(Class<MSG> msgClass, Function<FriendlyByteBuf, MSG> decoder, Optional<NetworkDirection> direction)
    {
        INSTANCE.registerMessage(id++, msgClass, IPacket::toBytes, decoder, (t, ctx) ->
        {
            t.process(ctx);
            ctx.get().setPacketHandled(true);
        }, direction);
    }

}
