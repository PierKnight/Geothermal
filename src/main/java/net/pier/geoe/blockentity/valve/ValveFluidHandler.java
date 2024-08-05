package net.pier.geoe.blockentity.valve;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class ValveFluidHandler implements IFluidHandler, IValveHandler {

    private final IFluidHandler fluidHandler;

    private final ValveBlockEntity.Flow flow;


    public ValveFluidHandler(IFluidHandler fluidHandler, ValveBlockEntity.Flow flow) {
        this.flow = flow;
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
        return this.flow == ValveBlockEntity.Flow.INPUT ? this.fluidHandler.fill(resource, action) : 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.flow == ValveBlockEntity.Flow.OUTPUT ? this.fluidHandler.drain(resource, action) : FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.flow == ValveBlockEntity.Flow.OUTPUT ? this.fluidHandler.drain(maxDrain, action) : FluidStack.EMPTY;
    }


    @Override
    public ValveBlockEntity.Flow getFlow() {
        return this.flow;
    }
}
