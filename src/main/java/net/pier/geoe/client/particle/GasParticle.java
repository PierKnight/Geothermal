package net.pier.geoe.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GasParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    public GasParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet sprites, Fluid fluid) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.friction = 0.96F;
        float f = 2.5F;
        this.xd *= (double)0.1F;
        this.yd *= (double)0.1F;
        this.zd *= (double)0.1F;
        this.xd += pXSpeed;
        this.yd += pYSpeed;
        this.zd += pZSpeed;
        float f1 = 1.0F - (float)(Math.random() * (double)0.1F);

        float r = (float)(fluid.getAttributes().getColor() >> 16 & 255) / 255.0F;
        float g = (float)(fluid.getAttributes().getColor() >> 8 & 255) / 255.0F;
        float b = (float)(fluid.getAttributes().getColor() & 255) / 255.0F;

        this.rCol = r * f1;
        this.gCol = g * f1;
        this.bCol = b * f1;
        this.quadSize *= 1.875F;
        this.lifetime = (int)Math.max(5.0F + Math.random() * 15.0F, 1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        float ratio = (float) this.age / this.lifetime;
        this.alpha = 1.0F - ratio;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FluidParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(FluidParticleOption pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new GasParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites, pType.getFluid());
        }
    }
}
