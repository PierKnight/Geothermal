/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package net.pier.geoe.gui.info;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.pier.geoe.client.GuiHelper;

import java.util.List;

import static net.pier.geoe.util.TextUtils.applyFormat;

public class FluidInfoArea extends InfoArea
{
    private final IFluidTank tank;
    private final Rect2i area;

    public FluidInfoArea(IFluidTank tank, Rect2i area)
    {
        super(area);
        this.tank = tank;
        this.area = area;
    }


    public void draw(PoseStack transform)
    {
        FluidStack fluid = tank.getFluid();
        float capacity = tank.getCapacity();
        transform.pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        if(!fluid.isEmpty())
        {
            int fluidHeight = (int)(area.getHeight()*(fluid.getAmount()/capacity));
            GuiHelper.drawRepeatedFluidSpriteGui(buffer, transform, fluid, area.getX(), area.getY()+area.getHeight()-fluidHeight, area.getWidth(), fluidHeight);
        }

        buffer.endBatch();
        transform.popPose();
    }

    @Override
    protected void fillToolTip(List<Component> tooltip) {
        FluidStack fluid = this.tank.getFluid();

        if(!fluid.isEmpty())
            tooltip.add(applyFormat(
                    fluid.getDisplayName(),
                    fluid.getFluid().getAttributes().getRarity(fluid).color
            ));
        else
            tooltip.add(new TranslatableComponent("gui.geoe.empty"));

        if(Minecraft.getInstance().options.advancedItemTooltips && !fluid.isEmpty())
        {
            if(!Screen.hasShiftDown())
                tooltip.add(new TranslatableComponent("info.holdShiftForInfo"));
            else
            {
                tooltip.add(applyFormat(new TextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()), ChatFormatting.DARK_GRAY));
                tooltip.add(applyFormat(new TextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.add(applyFormat(new TextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.add(applyFormat(new TextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
                tooltip.add(applyFormat(new TextComponent("NBT Data: "+fluid.getTag()), ChatFormatting.DARK_GRAY));
            }
        }
        tooltip.add(new TextComponent(fluid.getAmount()+"/"+this.tank.getCapacity()+"mB"));
    }
}