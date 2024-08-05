package net.pier.geoe.register;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.gui.ExtractorMenu;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;

import java.util.Objects;
import java.util.function.Function;

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
