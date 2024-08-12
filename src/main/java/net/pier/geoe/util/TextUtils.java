package net.pier.geoe.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class TextUtils {

    public static MutableComponent applyFormat(Component component, ChatFormatting... color)
    {
        Style style = component.getStyle();
        for(ChatFormatting format : color)
            style = style.applyFormat(format);
        return component.copy().setStyle(style);
    }

    public static void addFluidStackTooltip(FluidStack fluid, List<Component> tooltip, int capacity)
    {
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
        tooltip.add(new TextComponent(fluid.getAmount()+"/"+ capacity+"mB"));
    }
}
