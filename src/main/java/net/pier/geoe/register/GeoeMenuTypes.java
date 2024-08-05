package net.pier.geoe.register;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.blockentity.ExtractorBlockEntity;
import net.pier.geoe.gui.ExtractorMenu;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class GeoeMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registry.MENU_REGISTRY, Geothermal.MODID);

    public static final RegistryObject<MenuType<ExtractorMenu>> EXTRACTOR = register("extractor", context -> new ExtractorMenu( context.windowId(), context.inv(), context.be()));

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, A extends GeoeContainerMenu<T>> RegistryObject<MenuType<A>> register(String name, Function<MenuContext<T>, A> supplier)
    {
         return MENU_TYPES.register(name, () -> new MenuType<>((IContainerFactory<A>) (windowId, inv, data) -> {
             T be = (T) Objects.requireNonNull(inv.player.level.getBlockEntity(data.readBlockPos()), "Tile Entity Missing!");
             return supplier.apply(new MenuContext<>(windowId, inv, be));
         }));
    }




}
