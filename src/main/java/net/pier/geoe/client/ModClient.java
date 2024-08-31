package net.pier.geoe.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.client.particle.GasParticle;
import net.pier.geoe.client.render.TemplateMultiBlockRenderer;
import net.pier.geoe.gui.WellScreen;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeMenuTypes;
import net.pier.geoe.register.GeoeParticleTypes;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClient
{


    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
                    ItemBlockRenderTypes.setRenderLayer(GeoeBlocks.GLASS.get(), RenderType.cutout());
                    GeoeBlocks.VALVES_BLOCK.values().forEach(blockRegistryObject -> ItemBlockRenderTypes.setRenderLayer(blockRegistryObject.get(), RenderType.cutout()));
                    //Controller
                    for (RegistryObject<Block> entry : GeoeBlocks.REGISTER.getEntries())
                        if(entry.get() instanceof ControllerBlock<?> controllerBlock)
                            ItemBlockRenderTypes.setRenderLayer(controllerBlock, RenderType.cutout());
                    //SCREENS
                    MenuScreens.register(GeoeMenuTypes.EXTRACTOR.get(), WellScreen::new);
                }
        );
    }



    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(GeoeParticleTypes.GAS_TYPE.get(), GasParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenders(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(GeoeBlocks.PRODUCTION_WELL_BE.get(), TemplateMultiBlockRenderer::new);
        event.registerBlockEntityRenderer(GeoeBlocks.INJECTION_WELL_BE.get(), TemplateMultiBlockRenderer::new);
    }

}
