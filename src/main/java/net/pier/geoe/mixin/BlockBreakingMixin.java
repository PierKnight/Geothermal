package net.pier.geoe.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Block.class)
public class BlockBreakingMixin {


    @Inject(at = @At("HEAD"), method = "animateTick")
    private void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRandom, CallbackInfo ci)
    {
        if(pLevel instanceof ClientLevel)
        {
            ChunkPos chunkPos = new ChunkPos(pPos);
            pLevel.getCapability(ReservoirCapability.CAPABILITY).ifPresent(cap -> {
                if(cap.isReservoirDirty(chunkPos) && cap.getReservoir(chunkPos).getEarthquakeTime() >= 0.0F)
                {
                    for (Direction direction : Direction.values()) {
                        if(pLevel.getBlockState(pPos.relative(direction)).isAir()) {
                            //Vec3 vec3i = new Vec3(pPos.getX(), pPos.getY(), pPos.getZ()).add(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
                            Minecraft.getInstance().particleEngine.crack(pPos, direction);
                        }
                    }
                }
            });
        }
    }
}
