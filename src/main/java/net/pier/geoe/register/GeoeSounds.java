package net.pier.geoe.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;

public class GeoeSounds {

    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Geothermal.MODID);
    public static final RegistryObject<SoundEvent> GAS_LEAK = registerEvent("gas_leak", "block.geothermal_pipe.leak");


    private static RegistryObject<SoundEvent> registerEvent(String name, String path)
    {
        return REGISTER.register(name, () -> new SoundEvent(new ResourceLocation(Geothermal.MODID,path)));
    }
}
