package net.pier.geoe.blockentity.valve;

import net.minecraftforge.energy.IEnergyStorage;

public class ValveEnergyHandler implements IEnergyStorage,IInputHandler {

    private final IEnergyStorage energyStorage;
    private final boolean input;

    public ValveEnergyHandler(IEnergyStorage energyStorage, boolean input) {
        this.energyStorage = energyStorage;
        this.input = input;
    }

    public boolean isInput() {
        return input;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.input ? this.energyStorage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return !this.input ? this.energyStorage.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return this.energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return !this.input;
    }

    @Override
    public boolean canReceive() {
        return this.input;
    }
}
