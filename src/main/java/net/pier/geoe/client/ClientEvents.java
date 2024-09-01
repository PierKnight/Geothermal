package net.pier.geoe.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.pier.geoe.Geothermal;
import net.pier.geoe.capability.reservoir.ReservoirCapability;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {




    @SubscribeEvent
    public static void camera(final EntityViewRenderEvent.CameraSetup event)
    {

        ClientLevel clientLevel = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        Camera camera = event.getCamera();

        if(clientLevel != null && !Minecraft.getInstance().isPaused() && (player == null || player.isOnGround()))
        {
            float nearestEarthquake = 0F;
            double chunkDistance = Float.MAX_VALUE;

            for(int i = -2;i <= 2;i++)
            {
                for (int j = -2; j <= 2; j++) {
                    int chunkX = SectionPos.blockToSectionCoord(camera.getBlockPosition().getX()) + i;
                    int chunkZ = SectionPos.blockToSectionCoord(camera.getBlockPosition().getZ()) + j;

                    var optional = clientLevel.getCapability(ReservoirCapability.CAPABILITY).resolve();
                    if(optional.isPresent())
                    {
                        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                        float earthquakeAmount = optional.get().isReservoirDirty(chunkPos) ? optional.get().getReservoir(chunkPos).getEarthquakeTime() : -1.0F;
                        double distance = camera.getPosition().distanceToSqr(SectionPos.sectionToBlockCoord(chunkX) + 8, camera.getPosition().y, SectionPos.sectionToBlockCoord(chunkZ) + 8);
                        if(earthquakeAmount >= 0.0F && distance < chunkDistance)
                        {
                            nearestEarthquake = earthquakeAmount;
                            chunkDistance = distance;
                        }
                    }
                }
            }
            if(nearestEarthquake != Float.MAX_VALUE)
            {
                nearestEarthquake *= (float) (256F / Math.max(chunkDistance, 256F));
                nearestEarthquake *= nearestEarthquake;
                float angle = ((float) event.getPartialTicks() + clientLevel.getDayTime());
                float angleYaw = Mth.sin(angle*0.8F + 0.3F) * 3F + (clientLevel.random.nextFloat() - 0.5F) * 0.3F;
                float anglePitch = Mth.sin(angle*1.2F) * 3F + (clientLevel.random.nextFloat() - 0.5F) * 0.3F;
                float angleRoll = Mth.sin(angle*1.8F + 0.8F) * 3F + (clientLevel.random.nextFloat() - 0.5F) * 0.3F;

                event.setYaw(event.getYaw() + angleYaw * nearestEarthquake);
                event.setPitch(event.getPitch() + anglePitch * nearestEarthquake);
                event.setRoll(event.getRoll() + angleRoll * nearestEarthquake);
            }
        }
    }
}
