package net.pier.geoe.advancement.triggers;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.pier.geoe.Geothermal;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EarthquakeTrigger extends SimpleCriterionTrigger<EarthquakeTrigger.TriggerInstance> {

    static final ResourceLocation ID = new ResourceLocation(Geothermal.MODID, "earthquake");

    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject pJson, EntityPredicate.Composite pPlayer, DeserializationContext pContext) {
        return new EarthquakeTrigger.TriggerInstance(pPlayer);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public void trigger(ServerPlayer pPlayer) {
        this.trigger(pPlayer, (triggerInstance) -> triggerInstance.matches(pPlayer));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(EntityPredicate.Composite pPlayer) {
            super(ID, pPlayer);
        }

        public boolean matches(Player player) {

            Optional<ReservoirCapability> optional = player.level.getCapability(ReservoirCapability.CAPABILITY).resolve();
            if(optional.isPresent())
            {
                ReservoirCapability reservoirCapability = optional.get();
                if(reservoirCapability.isReservoirDirty(player.chunkPosition()))
                    return reservoirCapability.getReservoir(player.chunkPosition()).getEarthquakeTime() >= 0.0F;
            }
            return false;
        }
    }
}
