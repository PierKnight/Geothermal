package net.pier.geoe.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.pier.geoe.Geothermal;
import net.pier.geoe.capability.CapabilityInitializer;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.capability.reservoir.ReservoirSampler;
import net.pier.geoe.register.GeoeItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;


@ParametersAreNonnullByDefault
public class ReservoirMap extends Item {

    private static final MaterialColor.Brightness[] ORDERED_BRIGHTNESS = new MaterialColor.Brightness[]{MaterialColor.Brightness.LOWEST,MaterialColor.Brightness.LOW,MaterialColor.Brightness.NORMAL,MaterialColor.Brightness.HIGH};

    public ReservoirMap() {
        super(new Properties().stacksTo(1).tab(Geothermal.CREATIVE_TAB));
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        BlockPos pos = getPositionFromMap(pStack);
        MapType mapType = getMapType(pStack);
        if(pos != null)
        {
            pTooltipComponents.add(new TextComponent("X: " + pos.getX()));
            pTooltipComponents.add(new TextComponent("Z: " + pos.getZ()));
        }
        if(mapType != null)
            pTooltipComponents.add(new TextComponent("Map Type: " + mapType.name()));
        else if(pos == null)
            pTooltipComponents.add(new TextComponent("Right click to set coordinates"));
        else
            pTooltipComponents.add(new TextComponent("Some trader can be interested to study this location."));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {

        MapType mapType = getMapType(pStack);
        BlockPos pos = getPositionFromMap(pStack);

        if(mapType != null && pos != null && pEntity instanceof Player player)
        {
            player.getInventory().setItem(pSlotId, newGeothermalMap(pLevel, pos.getX(), pos.getZ(), mapType));
        }


    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (this.allowdedIn(pCategory)) {
            pItems.add(new ItemStack(this));
            for (MapType mapType : MapType.values()) {
                ItemStack reservoirMap = new ItemStack(this);
                setReservoirMapType(reservoirMap, mapType);
                pItems.add(reservoirMap);
            }
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(!pLevel.isClientSide)
            pPlayer.level.playSound(null, pPlayer, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, pPlayer.getSoundSource(), 1.0F, 1.0F);
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        setReservoirMapPos(stack, pPlayer.getBlockX(), pPlayer.getBlockZ());
        return InteractionResultHolder.consume(stack);
    }

    public static void setReservoirMapType(ItemStack stack,MapType mapType)
    {
        if(!stack.is(GeoeItems.RESERVOIR_MAP.get()))
            return;
        stack.getOrCreateTag().putString("mapType", mapType.name());
    }

    public static void setReservoirMapPos(ItemStack stack, int posX, int posZ)
    {
        if(!stack.is(GeoeItems.RESERVOIR_MAP.get()))
            return;

        stack.getOrCreateTag().putInt("mapX", posX);
        stack.getOrCreateTag().putInt("mapZ", posZ);
    }


    @Nullable
    public static BlockPos getPositionFromMap(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if(stack.is(GeoeItems.RESERVOIR_MAP.get()) && tag != null && tag.contains("mapX") && tag.contains("mapZ"))
        {
            return new BlockPos(tag.getInt("mapX"),0,tag.getInt("mapZ"));
        }
        return null;
    }

    @Nullable
    public static MapType getMapType(ItemStack stack)
    {
        if(stack.is(GeoeItems.RESERVOIR_MAP.get()) && stack.getTag() != null && stack.getTag().contains("mapType"))
        {
            try {
                return MapType.valueOf(stack.getTag().getString("mapType"));
            }
            catch (Exception ignored){}
        }
        return null;
    }


    private static ItemStack newGeothermalMap(Level level, int posX, int posZ, MapType mapType)
    {

        ItemStack newMap = MapItem.create(level, posX, posZ, (byte)4, true, false);
        newMap.setHoverName(new TextComponent("Geothermal Map: " + mapType.name()));
        newMap.getOrCreateTagElement("display").putInt("MapColor", mapType.color);
        newMap.getOrCreateTag().putBoolean("geothermalMap", true);
        MapItemSavedData data = MapItem.getSavedData(newMap, level);

        ReservoirCapability capability = CapabilityInitializer.getCap(level, ReservoirCapability.CAPABILITY);

        MaterialColor[] materialColorList = new MaterialColor[]{MaterialColor.COLOR_PURPLE,MaterialColor.COLOR_RED,MaterialColor.COLOR_ORANGE,MaterialColor.COLOR_YELLOW};

        float bestValue = -1;
        ChunkPos bestChunk = ChunkPos.ZERO;

        if(data != null && capability != null && level instanceof ServerLevel serverLevel) {
            int d = 1 << data.scale;
            int l = data.x / d - 64;
            int i1 = data.z / d - 64;
            for (int i = 0; i < 128; i++) {
                for (int j = 0; j < 128; j++) {
                    int chunkX = SectionPos.blockToSectionCoord((l + i) * d);//SectionPos.blockToSectionCoord(data.x) + i;
                    int chunkZ = SectionPos.blockToSectionCoord((i1 + j) * d);//SectionPos.blockToSectionCoord(data.z) + j;

                    ReservoirSampler.Sample sample = capability.reservoirSampler.getSample(serverLevel, chunkX, chunkZ);
                    float mapValue = mapType.value.apply(sample);
                    int val = (int) Mth.clampedMap(mapValue,-1.0D, 1.0F,0,materialColorList.length * 4 - 1);
                    MaterialColor materialColor = materialColorList[val / 4];
                    MaterialColor.Brightness brightness = ORDERED_BRIGHTNESS[val % 4];
                    data.setColor(i, j, materialColor.getPackedId(brightness));
                    if(mapValue > bestValue) {
                        bestValue = mapValue;
                        bestChunk = new ChunkPos(chunkX, chunkZ);
                    }
                }
            }
        }

        MapItemSavedData.addTargetDecoration(newMap,bestChunk.getMiddleBlockPosition(64), "best_point", MapDecoration.Type.RED_X);

        MapItem.lockMap(level, newMap);
        return newMap;
    }

    public enum MapType
    {
        DEPTH(0XFF0000, ReservoirSampler.Sample::depth),
        SIZE(0X00FF00, ReservoirSampler.Sample::size),
        HEAT(0X00FF0F, ReservoirSampler.Sample::heat),
        STEAM(0XFFFFFF, sample -> sample.type() == Reservoir.Type.GEOTHERMAL ? -1F : 1F);

        private final int color;
        private final Function<ReservoirSampler.Sample, Float> value;

        MapType(int color, Function<ReservoirSampler.Sample, Float> value) {
            this.color = color;
            this.value = value;
        }

        public int getColor() {
            return color;
        }
    }
}
