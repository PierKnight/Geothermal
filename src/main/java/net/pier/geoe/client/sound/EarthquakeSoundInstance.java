package net.pier.geoe.client.sound;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.register.GeoeSounds;

public class EarthquakeSoundInstance extends AbstractTickableSoundInstance {

    private final ChunkPos chunkPos;

    protected EarthquakeSoundInstance(ChunkPos chunkPos) {
        super(GeoeSounds.EARTHQUAKE.get(), SoundSource.AMBIENT);
        this.looping = true;
        this.chunkPos = chunkPos;


    }

    @Override
    public void tick() {

        ClientLevel clientLevel = Minecraft.getInstance().level;
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;

        this.x = SectionPos.sectionToBlockCoord(chunkPos.x) + 8;
        this.y = camera.getPosition().y;
        this.z = SectionPos.sectionToBlockCoord(chunkPos.z) + 8;

        double distance = Math.sqrt(this.x * camera.getPosition().x + this.z * camera.getPosition().z);

        if(clientLevel == null)
            this.stop();
        else
        {
            clientLevel.getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> {
                float earthquakeAmount = cap.getReservoir(chunkPos).getEarthquakeTime();
                if(earthquakeAmount >= 0.0F)
                    this.volume = 1.0F * earthquakeAmount;
                else
                    this.stop();
            });
        }
    }
}
