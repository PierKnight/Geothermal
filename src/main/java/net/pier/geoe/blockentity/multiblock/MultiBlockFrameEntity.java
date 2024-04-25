package net.pier.geoe.blockentity.multiblock;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.blockentity.BaseBlockEntity;
import org.lwjgl.opengl.GL14;

public abstract class MultiBlockFrameEntity extends BaseBlockEntity
{

    private BlockPos controllerPos;

    public MultiBlockFrameEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }


    @Override
    public void readTag(CompoundTag tag)
    {

    }

    @Override
    public void writeTag(CompoundTag tag)
    {

    }

}
