package net.pier.geoe;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.pier.geoe.capability.CapabilityInitializer;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeConfiguredFeatures;
import net.pier.geoe.register.GeoeFeatures;
import net.pier.geoe.register.GeoeItems;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Geothermal.MODID)
public class Geothermal
{

    public static final String MODID = "geoe";
    public static final String MODNAME = "Geothermal";
    public static final String MODVERSION = ModList.get().getModFileById(MODID).versionString();

    // Directly reference a slf4j logger
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


    public static final Map<ChunkPos, Map<BlockPos, FluidStack>> openedPipes = new HashMap<>();

    public Geothermal()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(CapabilityInitializer.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CapabilityInitializer::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CapabilityInitializer::registerRenders);


        GeoeFeatures.init();
        GeoeBlocks.init();
        GeoeItems.init();

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


}
