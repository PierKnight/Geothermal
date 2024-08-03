package net.pier.geoe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.pier.geoe.block.BlockMachineFrame;
import net.pier.geoe.register.GeoeBlocks;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CTMTextureProvider implements DataProvider {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, Builder> builders = new LinkedHashMap<>();

    private final DataGenerator gen;

    public CTMTextureProvider(DataGenerator gen) {
        this.gen = gen;
    }

    private void register()
    {
        ResourceLocation frameTexture = new ResourceLocation(Geothermal.MODID, "block/frame_ctm");
        ResourceLocation glassTexture = new ResourceLocation(Geothermal.MODID, "block/glass_ctm");

        Predicate<Boolean> completePredicate = new Predicate<>(BlockMachineFrame.COMPLETE, true);

        var conditions = GeoeBlocks.REGISTER.getEntries().stream().map(blockRegistryObject -> blockRegistryObject.get().defaultBlockState()).filter(
                blockState -> blockState.getOptionalValue(BlockMachineFrame.COMPLETE).isPresent() && !blockState.is(GeoeBlocks.GLASS.get())
        ).map(blockState -> new ConnectionCondition(blockState.getBlock(), completePredicate)).toArray(ConnectionCondition[]::new);

        builders.put("frame", new Builder().renderType(RenderType.CUTOUT).textures(frameTexture).connectTo(conditions));
        builders.put("glass", new Builder().renderType(RenderType.CUTOUT).textures(glassTexture).connectTo(new ConnectionCondition(GeoeBlocks.GLASS.get(), completePredicate)));
    }

    @Override
    public void run(@NotNull HashCache pCache) throws IOException {

        register();
        for (var entry : builders.entrySet()) {
            save(pCache, entry.getValue().build(), this.gen.getOutputFolder().resolve("assets/" + Geothermal.MODID + "/textures/block/" + entry.getKey() + ".png.mcmeta"));
        }
    }

    private void save(HashCache cache, Object object, Path target) throws IOException {
        String data = GSON.toJson(object);
        data = JavaUnicodeEscaper.outsideOf(0, 0x7f).translate(data); // Escape unicode after the fact so that it's not double escaped by GSON
        String hash = DataProvider.SHA1.hashUnencodedChars(data).toString();
        if (!Objects.equals(cache.getHash(target), hash) || !Files.exists(target)) {
            Files.createDirectories(target.getParent());

            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(target)) {
                bufferedwriter.write(data);
            }
        }

        cache.putNew(target, hash);
    }

    @NotNull
    @Override
    public String getName() {
        return "CTMTextureProvider" + Geothermal.MODID;
    }

    static class Builder {

        private int version = 1;
        private int layer = 1;
        private RenderType renderType = RenderType.SOLID;

        private ResourceLocation[] textures = new ResourceLocation[0];

        private ConnectionCondition[] connectTo = new ConnectionCondition[0];

        public Builder version(int version)
        {
            this.version = version;
            return this;
        }

        public Builder layer(int layer)
        {
            this.layer = layer;
            return this;
        }

        public Builder renderType(RenderType renderType)
        {
            this.renderType = renderType;
            return this;
        }

        public Builder textures(ResourceLocation... textures)
        {
            this.textures = textures;
            return this;
        }

        public Builder connectTo(ConnectionCondition... connectTo)
        {
            this.connectTo = connectTo;
            return this;
        }

        public JsonObject build()
        {
            JsonObject jsonObject = new JsonObject();

            JsonObject ctm = new JsonObject();
            jsonObject.add("ctm", ctm);

            ctm.addProperty("ctm_version", 1);
            ctm.addProperty("type", "ctm");
            ctm.addProperty("layer", renderType.name());

            JsonArray texturesArray = new JsonArray();
            ctm.add("textures", texturesArray);

            for (ResourceLocation texture : textures)
                texturesArray.add(texture.toString());

            JsonObject extra = new JsonObject();
            ctm.add("extra", extra);
            JsonArray connectToArray = new JsonArray();
            extra.add("connect_to", connectToArray);

            for (ConnectionCondition connect : this.connectTo) {
                JsonObject connectState = new JsonObject();
                connectState.addProperty("block", connect.block.getRegistryName().toString());
                JsonArray predicates = new JsonArray();

                for (var predicate : connect.properties) {
                    JsonObject predicateObject = new JsonObject();
                    predicateObject.addProperty(predicate.property.getName(), predicate.value.toString());
                    predicateObject.addProperty("compare_func", "=");
                    predicates.add(predicateObject);
                }
                connectState.add("predicate", predicates);

                connectToArray.add(connectState);
            }
            return jsonObject;
        }

    }

    enum RenderType
    {
        SOLID, CUTOUT, CUTOUT_MIPPED, TRANSLUCENT;
    }

    record ConnectionCondition(Block block, Predicate<?>... properties){}

    record Predicate<T extends Comparable<T>>(Property<T> property, T value){}

}
