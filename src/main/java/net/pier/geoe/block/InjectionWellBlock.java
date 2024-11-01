package net.pier.geoe.block;

import net.pier.geoe.blockentity.WellBlockEntity;

public class InjectionWellBlock extends ControllerBlock<WellBlockEntity>
{
    public InjectionWellBlock(Properties pProperties)
    {
        super(pProperties, WellBlockEntity.Injection::new);
    }
}
