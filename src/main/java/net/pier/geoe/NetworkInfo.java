package net.pier.geoe;

import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

public class NetworkInfo {

    public int totalPipes = 0;
    private FluidStack fluidStack;


    public NetworkInfo(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }
}
