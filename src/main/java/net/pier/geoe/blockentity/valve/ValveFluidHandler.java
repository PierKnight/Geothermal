package net.pier.geoe.blockentity.valve;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ValveFluidHandler implements IFluidHandler, IValveHandler {

    private final Supplier<IFluidHandler> fluidHandler;

    private final ValveBlockEntity.Flow flow;


    public ValveFluidHandler(Supplier<IFluidHandler> fluidHandler, ValveBlockEntity.Flow flow) {
        this.flow = flow;
        this.fluidHandler = fluidHandler;


    }


    private <T> T getOrElse(Function<IFluidHandler, T> function, T empty)
    {
        return this.fluidHandler.get() != null ? function.apply(this.fluidHandler.get()) : empty;
    }

    @Override
    public int getTanks() {
        return getOrElse(IFluidHandler::getTanks, 0);
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getOrElse(f -> f.getFluidInTank(tank), FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return getOrElse(f -> f.getTankCapacity(tank), 0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getOrElse(f -> f.isFluidValid(tank, stack), false);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.flow == ValveBlockEntity.Flow.INPUT ? getOrElse(f -> f.fill(resource, action), 0) : 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.flow == ValveBlockEntity.Flow.OUTPUT ? getOrElse(f -> f.drain(resource, action), FluidStack.EMPTY) : FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.flow == ValveBlockEntity.Flow.OUTPUT ? getOrElse(f -> f.drain(maxDrain, action), FluidStack.EMPTY) : FluidStack.EMPTY;
    }


    @Override
    public ValveBlockEntity.Flow getFlow() {
        return this.flow;
    }
}
