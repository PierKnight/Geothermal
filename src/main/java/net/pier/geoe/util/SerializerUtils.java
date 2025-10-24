package net.pier.geoe.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SerializerUtils {


    public static <K, V> ListTag mapToNBT(Map<K, V> map, Function<K, CompoundTag> keySerializer, Function<V, CompoundTag> valueSerializer)
    {
        ListTag mapList = new ListTag();
        map.forEach((key, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("key", keySerializer.apply(key));
            entry.put("value", valueSerializer.apply(value));
            mapList.add(entry);
        });
        return mapList;
    }

    public static <K, V> Map<K, V> nbtToMap(ListTag listTag, Function<CompoundTag, K> keyDeserializer, Function<CompoundTag, V> valueDeserializer)
    {
        Map<K, V> newMap = new HashMap<>();

        for (int i = 0; i < listTag.size(); i++)
        {
            CompoundTag tag = listTag.getCompound(i);
            newMap.put(keyDeserializer.apply(tag.getCompound("key")), valueDeserializer.apply(tag.getCompound("value")));
        }
        return newMap;
    }

}
