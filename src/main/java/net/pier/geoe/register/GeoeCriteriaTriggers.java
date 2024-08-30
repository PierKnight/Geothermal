package net.pier.geoe.register;

import net.minecraft.advancements.CriteriaTriggers;
import net.pier.geoe.advancement.triggers.EarthquakeTrigger;

public class GeoeCriteriaTriggers {

    public static EarthquakeTrigger EARTHQUAKE_TRIGGER;


    public static void preInit()
    {
        EARTHQUAKE_TRIGGER = CriteriaTriggers.register(new EarthquakeTrigger());
    }
}
