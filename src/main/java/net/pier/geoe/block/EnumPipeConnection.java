package net.pier.geoe.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum EnumPipeConnection implements StringRepresentable
{
    NONE("none"),
    INPUT("input"),
    OUTPUT("output"),
    PIPE("pipe");


    public static final EnumPipeConnection[] CONNECTIONS = EnumPipeConnection.values();

    private final String name;

    EnumPipeConnection(String name)
    {
        this.name = name;
    }

    public boolean isConnected()
    {
        return this != NONE;
    }

    public EnumPipeConnection rotateFluidConnection()
    {
        return CONNECTIONS[(this.ordinal() + 1) % (CONNECTIONS.length - 1)];
    }

    public boolean isTankConnection()
    {
        return this == INPUT || this == OUTPUT;
    }

    public static EnumPipeConnection indexOf(int index)
    {
        return CONNECTIONS[index];
    }

    @Override
    @NotNull
    public String getSerializedName()
    {
        return this.name;
    }
}
