package net.pier.geoe.gui;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.pier.geoe.blockentity.ExtractorBlockEntity;
import net.pier.geoe.capability.ReservoirCapability;
import net.pier.geoe.capability.world.Reservoir;
import net.pier.geoe.gui.data.DataContainerType;
import net.pier.geoe.gui.data.GenericProperty;
import net.pier.geoe.register.GeoeMenuTypes;

public class ExtractorMenu extends GeoeContainerMenu<ExtractorBlockEntity> {



    public Reservoir reservoir;
    public ExtractorMenu(int pContainerId, Inventory pPlayerInventory, ExtractorBlockEntity entity) {
        super(GeoeMenuTypes.EXTRACTOR.get(), pContainerId, pPlayerInventory, entity);


        if(entity.getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> reservoir = cap.getReservoir(new ChunkPos(entity.getBlockPos())));

        this.properties.add(GenericProperty.ofTank(entity.tank));
        this.properties.add(GenericProperty.ofEnergy(entity.energyStorage));
        this.properties.add(new GenericProperty<>(DataContainerType.RESERVOIR, () -> reservoir, (r) -> reservoir = r));
    }

}
