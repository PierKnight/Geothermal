package net.pier.geoe.block;

import net.pier.geoe.blockentity.WellBlockEntity;

public class ProductionWellBlock extends ControllerBlock<WellBlockEntity>
{
    public ProductionWellBlock(Properties pProperties)
    {
        super(pProperties, WellBlockEntity.Production::new);
    }

}
