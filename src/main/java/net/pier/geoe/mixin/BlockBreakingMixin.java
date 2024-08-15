package net.pier.geoe.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
        if(pLevel instanceof ClientLevel && SectionPos.blockToSectionCoord(pPos.getX()) % 3 == 0 && SectionPos.blockToSectionCoord(pPos.getZ()) % 3 == 0)
        {
            for (Direction direction : Direction.values()) {
                if(pLevel.getBlockState(pPos.relative(direction)).isAir()) {
                    //Vec3 vec3i = new Vec3(pPos.getX(), pPos.getY(), pPos.getZ()).add(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
                    Minecraft.getInstance().particleEngine.crack(pPos, direction);
                }
            }
        }
    }
}
