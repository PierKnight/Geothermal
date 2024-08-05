package net.pier.geoe.gui.data;

import net.minecraft.nbt.IntTag;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericProperty<T> {

    private final DataContainerType.Serializer<T> serializer;
    public final Supplier<T> getter;

    private final Consumer<T> setter;

    private T value = null;

    public GenericProperty(DataContainerType.Serializer<T> serializer, Supplier<T> getter, Consumer<T> setter) {
        this.serializer = serializer;
        this.getter = getter;
        this.setter = setter;
    }

    public static GenericProperty<FluidStack> ofTank(FluidTank tank)
    {
        return new GenericProperty<>(DataContainerType.FLUID_STACK, tank::getFluid, tank::setFluid);
    }

    public static GenericProperty<Integer> ofEnergy(EnergyStorage energyStorage)
    {
        return new GenericProperty<>(DataContainerType.INTEGER, energyStorage::getEnergyStored, integer -> energyStorage.deserializeNBT(IntTag.valueOf(integer)));
    }


    public boolean isDirty()
    {
        T newValue = getter.get();
        if(newValue==null && value==null)
            return false;
        if(value!=null && newValue!=null && serializer.equals().test(value, newValue))
            return false;
        value = serializer.copy().apply(newValue);
        return true;
    }

    public void processSync(Object receivedData)
    {
        value = (T)receivedData;
        this.setter.accept(serializer.copy().apply(value));
    }

    public DataContainerType.DataPair<T> dataPair()
    {
        return new DataContainerType.DataPair<>(serializer, value);
    }






}
