package net.pier.geoe.advancement.triggers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.pier.geoe.register.GeoeCriteriaTriggers;

public class AdvancementEvents {


    @SubscribeEvent
    public static void playerEarthquake(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer serverPlayer && event.player.tickCount % 20 == 0)
            GeoeCriteriaTriggers.EARTHQUAKE_TRIGGER.trigger(serverPlayer);
    }
}
