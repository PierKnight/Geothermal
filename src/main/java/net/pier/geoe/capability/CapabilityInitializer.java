package net.pier.geoe.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.Geothermal;
import net.pier.geoe.capability.pipe.WorldNetworkCapability;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.network.PacketReservoirSync;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CapabilityInitializer
{

    @SubscribeEvent
    public static void capabilityAttachLevel(final AttachCapabilitiesEvent<Level> event)
    {
        attachCapability("pipe_network", event, WorldNetworkCapability::new, WorldNetworkCapability.CAPABILITY);
        attachCapability("geo_reservoir", event, () -> new ReservoirCapability(event.getObject()), ReservoirCapability.CAPABILITY);
    }

    @SubscribeEvent
    public static void capabilityAttachChunk(final AttachCapabilitiesEvent<LevelChunk> event)
    {

    }

    @SubscribeEvent
    public static void chunkLoad(final ChunkEvent.Load event)
    {
        if(event.getWorld() instanceof ServerLevel && event.getChunk() instanceof LevelChunk levelChunk)
            levelChunk.getLevel().getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> cap.appendTickingReservoir(levelChunk.getPos()));
    }

    @SubscribeEvent
    public static void chunkUnload(final ChunkEvent.Unload event)
    {
        if(event.getWorld() instanceof ServerLevel && event.getChunk() instanceof LevelChunk levelChunk)
            levelChunk.getLevel().getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> cap.tickingReservoirs.remove(levelChunk.getPos()));
    }



    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(WorldNetworkCapability.class);
        event.register(ReservoirCapability.class);
    }

    @SubscribeEvent
    public static void updateWorld(final TickEvent.WorldTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
            return;

        LazyOptional<WorldNetworkCapability> lazeCap = event.world.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(cap -> cap.tick(event.world));

        LazyOptional<ReservoirCapability> reservoirCapabilityLazy = event.world.getCapability(ReservoirCapability.CAPABILITY);
        reservoirCapabilityLazy.ifPresent(ReservoirCapability::tick);
    }

    @SubscribeEvent
    public static void updateWorldClinet(final TickEvent.ClientTickEvent event)
    {
        Level level = Minecraft.getInstance().level;

        if(event.phase == TickEvent.Phase.START || level == null || Minecraft.getInstance().isPaused())
            return;

        LazyOptional<ReservoirCapability> reservoirCapabilityLazy = level.getCapability(ReservoirCapability.CAPABILITY);
        reservoirCapabilityLazy.ifPresent(ReservoirCapability::tick);
    }

    @SubscribeEvent
    public static void chunkWatch(final ChunkWatchEvent.Watch event)
    {
        LazyOptional<ReservoirCapability> reservoirCapabilityLazy = event.getWorld().getCapability(ReservoirCapability.CAPABILITY);
        reservoirCapabilityLazy.ifPresent(cap -> {
            if(cap.isReservoirDirty(event.getPos()))
                PacketManager.INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer), new PacketReservoirSync(event.getPos(), cap.getReservoir(event.getPos()), PacketReservoirSync.Type.CHUNK_TRACK));
        });
    }
    @SubscribeEvent
    public static void chunkUnWatch(final ChunkWatchEvent.UnWatch event)
    {
        LazyOptional<ReservoirCapability> reservoirCapabilityLazy = event.getWorld().getCapability(ReservoirCapability.CAPABILITY);
        reservoirCapabilityLazy.ifPresent(cap -> {
            if(cap.isReservoirDirty(event.getPos()))
                PacketManager.INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer), new PacketReservoirSync(event.getPos(), cap.getReservoir(event.getPos()), PacketReservoirSync.Type.CHUNK_UNTRACK));
        });
    }

    private static <T extends INBTSerializable<Tag>> void attachCapability(String name, AttachCapabilitiesEvent<?> event, Supplier<T> supplier, Capability<T> c)
    {
        T capability = supplier.get();
        LazyOptional<T> mixtureOptional = LazyOptional.of(() -> capability);

        ICapabilityProvider provider = new ICapabilitySerializable<>()
        {
            @NotNull
            @Override
            public <A> LazyOptional<A> getCapability(@NotNull Capability<A> cap, @Nullable Direction side)
            {
                if(cap == c)
                    return mixtureOptional.cast();
                return LazyOptional.empty();
            }

            @Override
            public Tag serializeNBT()
            {
                return capability.serializeNBT();
            }

            @Override
            public void deserializeNBT(Tag nbt)
            {
                capability.deserializeNBT(nbt);
            }
        };
        event.addCapability(new ResourceLocation(Geothermal.MODID, name), provider);
    }


}
