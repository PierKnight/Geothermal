package net.pier.geoe.capability.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Predicate;

public class Reservoir implements IFluidTank {

    private final FluidTank inputTank;
    private final FluidTank outputTank;

    private final int capacity;
    private final int throughput;
    private final int temperature;
    private final Type type;


    public Reservoir(int capacity, int throughput,int temperature,Type type) {
        this.inputTank = new FluidTank(capacity, type.inputFluid);
        this.outputTank = new FluidTank(capacity);
        this.capacity = capacity;
        this.throughput = throughput;
        this.type = type;
        this.temperature = temperature;
    }

    public Reservoir(CompoundTag tag) {
        this.capacity = tag.getInt("capacity");
        this.type = Type.values()[tag.getInt("type")];
        this.inputTank = new FluidTank(capacity, type.inputFluid);
        this.outputTank = new FluidTank(capacity);
        this.throughput = tag.getInt("throughput");
        this.temperature = tag.getInt("temperature");
    }



    @NotNull
    @Override
    public FluidStack getFluid() {
        return this.outputTank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return this.outputTank.getFluidAmount();
    }

    @NotNull
    public FluidStack getInput() {
        return this.inputTank.getFluid();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getThroughput() {
        return throughput;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean isFluidValid(@NotNull FluidStack stack) {
        return this.type.inputFluid.test(stack);
    }

    public void update()
    {
        int conversionAmount = this.temperature + 1;

        int amount = this.inputTank.drain(conversionAmount, IFluidHandler.FluidAction.SIMULATE).getAmount();

        this.outputTank.setCapacity(this.capacity - this.inputTank.getFluidAmount());

        int fillAmount = this.outputTank.fill(new FluidStack(this.type.outputFluid,amount), IFluidHandler.FluidAction.EXECUTE);
        this.inputTank.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE).getAmount();
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action){
        this.inputTank.setCapacity(this.capacity - this.outputTank.getFluidAmount());
        return this.inputTank.fill(resource,action);
    }



    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        resource.setAmount(Math.min(this.throughput, resource.getAmount()));
        return this.outputTank.drain(resource,action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return this.outputTank.drain(Math.min(this.throughput, maxDrain),action);
    }


    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("capacity", this.capacity);
        tag.putInt("temperature", this.temperature);
        tag.putInt("throughput", this.throughput);
        tag.put("inputTank",this.inputTank.writeToNBT(new CompoundTag()));
        tag.put("outputTank",this.outputTank.writeToNBT(new CompoundTag()));
        tag.putInt("type", this.type.ordinal());
        return tag;
    }




    private static final TagKey<Fluid> STEAM_TAG = FluidTags.create(new ResourceLocation("forge", "steam"));
    private static final TagKey<Fluid> GEOTHERMAL_TAG = FluidTags.create(new ResourceLocation("forge", "geothermal_fluid"));
    public enum Type{
        STEAM(100, 100,Fluids.WATER),
        GEOTHERMAL( 100, 100,Fluids.WATER);

        public final Fluid outputFluid;
        public final Predicate<FluidStack> inputFluid;
        public final int inputAmount;
        public final int outputAmount;

        Type(Fluid outputFluid,Predicate<FluidStack> inputFluid, int inputAmount, int outputAmount) {
            this.inputFluid = inputFluid;
            this.inputAmount = inputAmount;
            this.outputAmount = outputAmount;
            this.outputFluid = outputFluid;
        }

        Type( int inputAmount, int outputAmount, Fluid outputFluid)
        {
            this(outputFluid,fluidStack -> fluidStack.getFluid().is(FluidTags.WATER),inputAmount,outputAmount);
        }
    }

}
