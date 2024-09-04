package net.pier.geoe.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.pier.geoe.item.ReservoirMap;
import net.pier.geoe.register.GeoeItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantContainer.class)
public class MerchantContainerMixing {


    @Shadow @Final private NonNullList<ItemStack> itemStacks;

    @Redirect(method = "updateSellItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;assemble()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack u(MerchantOffer instance)
    {
        ItemStack original = instance.assemble();
        if(original.is(GeoeItems.RESERVOIR_MAP.get()))
        {
            ItemStack mapInput = this.itemStacks.get(0).is(GeoeItems.RESERVOIR_MAP.get()) ? this.itemStacks.get(0) : this.itemStacks.get(1);
            BlockPos inputPos = ReservoirMap.getPositionFromMap(mapInput);
            if(inputPos != null) {
                ReservoirMap.setReservoirMapPos(original, inputPos.getX(), inputPos.getZ());
                return original;
            }
        }
        return original;

    }
}
