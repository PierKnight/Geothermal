package net.pier.geoe.blockentity.valve;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import org.jetbrains.annotations.NotNull;

public class ValveFluidHandler implements IFluidHandler,IInputHandler {

    private IFluidHandler fluidHandler;

    private final boolean input;

    public ValveFluidHandler(boolean input) {
        this.input = input;
    }

    public void setFluidHandler(IFluidHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public int getTanks() {
        return this.fluidHandler.getTanks();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.fluidHandler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.fluidHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.fluidHandler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.input ? this.fluidHandler.fill(resource, action) : 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return !this.input ? this.fluidHandler.drain(resource, action) : FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return !this.input ? this.fluidHandler.drain(maxDrain, action) : FluidStack.EMPTY;
    }

    @Override
    public boolean isInput() {
        return this.input;
    }
}
