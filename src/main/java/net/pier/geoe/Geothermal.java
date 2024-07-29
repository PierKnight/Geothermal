package net.pier.geoe;

import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.capability.CapabilityInitializer;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.network.PacketMultiBlockInfo;
import net.pier.geoe.register.*;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Geothermal.MODID)
public class Geothermal
{

    public static final String MODID = "geoe";
    public static final String MODNAME = "Geothermal";
    public static final String MODVERSION = ModList.get().getModFileById(MODID).versionString();

    private static final Logger LOGGER = LogUtils.getLogger();


    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab(MODID)
    {
        @Nonnull
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(Items.COPPER_INGOT);
        }
    };


    public static final Map<ChunkPos, Map<BlockPos, FluidStack>> networks = new HashMap<>();

    public Geothermal()
    {


        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::processIMC);
        eventBus.addListener(CapabilityInitializer::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(CapabilityInitializer.class);


        GeoeFeatures.FEATURES.register(eventBus);
        GeoeBlocks.REGISTER.register(eventBus);
        GeoeBlocks.BE_REGISTER.register(eventBus);
        GeoeItems.REGISTER.register(eventBus);
        GeoeParticleTypes.PARTICLE_TYPES.register(eventBus);
        GeoeSounds.REGISTER.register(eventBus);


    }

    private void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            // Some preinit code
            LOGGER.info("HELLO FROM PREINIT");
            LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

            PacketManager.init();
            GeoeConfiguredFeatures.init();
        });

    }


    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("Geothermal", "helloworld", () ->
        {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onServerStarting(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getPlayer() instanceof ServerPlayer serverPlayer)
        {

            LOGGER.info("LOGIN");
            PacketManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PacketMultiBlockInfo());
        }

    }



}
