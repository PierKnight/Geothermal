package net.pier.geoe.blockentity.valve;

import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Supplier;

public class LazyResetOptional<T>{


    private LazyOptional<T> lazyOptional;
    private final Supplier<LazyOptional<T>> supplier;

    public LazyResetOptional(Supplier<LazyOptional<T>> supplier) {
        this.supplier = supplier;
    }

    public LazyOptional<T> get()
    {
        if(this.lazyOptional == null)
            this.lazyOptional = this.supplier.get();
        return this.lazyOptional;
    }

    public void reset()
    {
        this.lazyOptional = null;
    }
}
