package net.pier.geoe.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.pier.geoe.item.ReservoirMap;
import net.pier.geoe.register.GeoeItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin {


    @Shadow @Final private ItemStack result;

    @Shadow protected abstract boolean isRequiredItem(ItemStack pOffer, ItemStack pCost);

    @Shadow public abstract ItemStack getCostA();

    @Inject(method = "satisfiedBy", at = @At("HEAD"), cancellable = true)
    public void reservoirMapSatisfation(ItemStack pPlayerOfferA, ItemStack pPlayerOfferB, CallbackInfoReturnable<Boolean> cir)
    {
        if(result.is(GeoeItems.RESERVOIR_MAP.get()))
        {

            if(pPlayerOfferB.is(GeoeItems.RESERVOIR_MAP.get()))
            {
                BlockPos pos = ReservoirMap.getPositionFromMap(pPlayerOfferB);
                cir.setReturnValue(pos != null && isRequiredItem(pPlayerOfferA,getCostA()) && pPlayerOfferA.getCount() >= getCostA().getCount());
            }
            else
                cir.setReturnValue(false);
        }
    }
}
