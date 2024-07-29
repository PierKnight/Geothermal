package net.pier.geoe.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.pier.geoe.Geothermal;
import net.pier.geoe.capability.world.WorldNetworkCapability;
import net.pier.geoe.client.TestRenderer;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CapabilityInitializer
{

    @SubscribeEvent
    public static void capabilityAttachLevel(final AttachCapabilitiesEvent<Level> event)
    {
        attachCapability("pipe_network", event, () -> new WorldNetworkCapability(), WorldNetworkCapability.CAPABILITY);
        attachCapability("geo_reservoir", event, () -> new ReservoirCapability(event.getObject()), ReservoirCapability.CAPABILITY);
    }

    @SubscribeEvent
    public static void capabilityAttachChunk(final AttachCapabilitiesEvent<LevelChunk> event)
    {

    }

    @SubscribeEvent
    public static void chunkLoad(final ChunkEvent.Load event)
    {
        if(event.getChunk() instanceof LevelChunk levelChunk && levelChunk.getLevel() instanceof ServerLevel level)
            levelChunk.getLevel().getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> cap.appendTickingReservoir(levelChunk.getPos()));
    }

    @SubscribeEvent
    public static void chunkUnload(final ChunkEvent.Unload event)
    {
        if(event.getChunk() instanceof LevelChunk levelChunk && levelChunk.getLevel() instanceof ServerLevel level)
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
        if(event.side == LogicalSide.CLIENT || event.phase == TickEvent.Phase.START)
            return;

        LazyOptional<WorldNetworkCapability> lazeCap = event.world.getCapability(WorldNetworkCapability.CAPABILITY);
        lazeCap.ifPresent(cap -> cap.tick(event.world));

        LazyOptional<ReservoirCapability> reservoirCapabilityLazy = event.world.getCapability(ReservoirCapability.CAPABILITY);
        reservoirCapabilityLazy.ifPresent(ReservoirCapability::tick);
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
