package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.FireballEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class FireballSpell extends SpellEffect {
    private static final float FIREBALL_SPEED = 1.5F;
    private static final float FIREBALL_DAMAGE = 24.0F; // 8d6 (avg 28, but balanced for Minecraft)
    private static final int EXPLOSION_RADIUS = 4; // 20ft radius (4 blocks)
    private static final int CAST_PARTICLE_COUNT = 20;
    private static final double CAST_PARTICLE_SPREAD = 0.5;
    private static final float SOUND_VOLUME = 1.2F;
    private static final float SOUND_PITCH = 0.9F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        FireballEntity fireball = new FireballEntity(ModEntityRegistry.FIREBALL_PROJECTILE.get(), level, caster);
        fireball.setPos(startPos.x, startPos.y, startPos.z);
        fireball.setDeltaMovement(lookDirection.scale(FIREBALL_SPEED));
        fireball.setDamage(FIREBALL_DAMAGE);
        fireball.setExplosionRadius(EXPLOSION_RADIUS);

        level.addFreshEntity(fireball);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.05);

            serverLevel.sendParticles(ParticleTypes.LAVA,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT / 4,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.02);
        }
    }

    @Override
    public String getName() {
        return "Fireball";
    }
}