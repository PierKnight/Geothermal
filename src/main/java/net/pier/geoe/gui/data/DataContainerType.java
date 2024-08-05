package net.pier.geoe.gui.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.capability.world.Reservoir;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class DataContainerType {


    private static final List<Serializer<?>> SERIALIZERS = new ArrayList<>();

    public static final Serializer<FluidStack> FLUID_STACK = register(
            FriendlyByteBuf::readFluidStack, FriendlyByteBuf::writeFluidStack,
            FluidStack::copy, FluidStack::isFluidStackIdentical);

    public static final Serializer<Integer> INTEGER = register(
            FriendlyByteBuf::readVarInt, FriendlyByteBuf::writeVarInt,
            Integer::valueOf, Integer::equals);

    public static final Serializer<Reservoir> RESERVOIR = register(buf -> new Reservoir(Objects.requireNonNull(buf.readNbt())),
            (buf, reservoir) -> buf.writeNbt(reservoir.save(new CompoundTag())),
            reservoir -> new Reservoir(reservoir.save(new CompoundTag())),
            (reservoir, reservoir2) -> FluidStack.areFluidStackTagsEqual(reservoir.getInput(), reservoir2.getInput()) && FluidStack.areFluidStackTagsEqual(reservoir.getFluid(), reservoir2.getFluid()));

    private static <T> Serializer<T> register(Function<FriendlyByteBuf, T> read,
                                              BiConsumer<FriendlyByteBuf, T> write,
                                              UnaryOperator<T> copy,
                                              BiPredicate<T, T> equals)
    {
        Serializer<T> serializer = new Serializer<T>(read, write, copy, equals, SERIALIZERS.size());
        SERIALIZERS.add(new Serializer<>(read, write, copy, equals, SERIALIZERS.size()));
        return serializer;
    }

    public static DataPair<?> read(FriendlyByteBuf buffer)
    {
        Serializer<?> serializer = SERIALIZERS.get(buffer.readVarInt());
        return serializer.read(buffer);
    }


    public record DataPair<T>(Serializer<T> serializer, T data)
    {
        public void write(FriendlyByteBuf to)
        {
            to.writeVarInt(serializer.id());
            serializer.write().accept(to, data);
        }
    }


    public record Serializer<T>(Function<FriendlyByteBuf, T> read,
                          BiConsumer<FriendlyByteBuf, T> write,
                          UnaryOperator<T> copy,
                          BiPredicate<T, T> equals, int id)
    {

        public DataPair<T> read(FriendlyByteBuf from)
        {
            return new DataPair<>(this, read().apply(from));
        }
    }
}
