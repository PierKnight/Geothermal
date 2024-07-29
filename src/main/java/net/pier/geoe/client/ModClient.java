package net.pier.geoe.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.pier.geoe.Geothermal;
import net.pier.geoe.client.particle.GasParticle;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeParticleTypes;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClient
{


    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(GeoeParticleTypes.GAS_TYPE.get(), GasParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenders(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(GeoeBlocks.TEST_BE.get(), TestRenderer::new);
    }

}
