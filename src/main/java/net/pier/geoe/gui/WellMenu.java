package net.pier.geoe.gui;

import net.minecraft.world.entity.player.Inventory;
import net.pier.geoe.blockentity.WellBlockEntity;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.gui.data.DataContainerType;
import net.pier.geoe.gui.data.GenericProperty;
import net.pier.geoe.register.GeoeMenuTypes;

public class WellMenu extends GeoeContainerMenu<WellBlockEntity> {



    public Reservoir reservoir;
    public WellMenu(int pContainerId, Inventory pPlayerInventory, WellBlockEntity entity) {
        super(GeoeMenuTypes.EXTRACTOR.get(), pContainerId, pPlayerInventory, entity);
        this.properties.add(GenericProperty.ofEnergy(entity.energyStorage));
        this.properties.add(new GenericProperty<>(DataContainerType.RESERVOIR, entity::getReservoir, (r) -> reservoir = r));
    }

}
