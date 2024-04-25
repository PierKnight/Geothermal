package net.pier.geoe.block;

import net.minecraft.util.StringRepresentable;

public enum EnumPipeConnection implements StringRepresentable
{
    NONE("none"),
    INPUT("input"),
    OUTPUT("output"),
    PIPE("pipe");


    public static final EnumPipeConnection[] CONNECTIONS = EnumPipeConnection.values();

    private final String name;

    private EnumPipeConnection(String name)
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
    public String getSerializedName()
    {
        return this.name;
    }
}
