package net.pier.geoe.block;

import net.pier.geoe.blockentity.DrillBlockEntity;

public class DrillControllerBlock extends ControllerBlock<DrillBlockEntity>{
    public DrillControllerBlock(Properties pProperties) {
        super(pProperties, DrillBlockEntity::new);
    }
}
