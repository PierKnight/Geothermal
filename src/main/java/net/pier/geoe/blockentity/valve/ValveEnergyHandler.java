package net.pier.geoe.blockentity.valve;

import net.minecraftforge.energy.IEnergyStorage;

public class ValveEnergyHandler implements IEnergyStorage, IValveHandler {

    private final IEnergyStorage energyStorage;
    private final ValveBlockEntity.Flow flow;

    public ValveEnergyHandler(IEnergyStorage energyStorage, ValveBlockEntity.Flow flow) {
        this.energyStorage = energyStorage;
        this.flow = flow;
    }


    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.flow == ValveBlockEntity.Flow.INPUT ? this.energyStorage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return this.flow == ValveBlockEntity.Flow.OUTPUT ? this.energyStorage.extractEnergy(maxExtract, simulate) : 0;
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
        return this.flow == ValveBlockEntity.Flow.OUTPUT;
    }

    @Override
    public boolean canReceive() {
        return this.flow == ValveBlockEntity.Flow.INPUT;
    }

    @Override
    public ValveBlockEntity.Flow getFlow() {
        return this.flow;
    }
}
