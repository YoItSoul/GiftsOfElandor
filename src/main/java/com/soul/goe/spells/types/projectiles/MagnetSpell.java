package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.projectiles.MagnetSpellEntity;
import com.soul.goe.spells.util.SpellEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MagnetSpell extends SpellEffect {

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        MagnetSpellEntity magnetSpell = new MagnetSpellEntity(ModEntityRegistry.MAGNET_PROJECTILE.get(), level, caster);
        magnetSpell.setPos(startPos.x, startPos.y, startPos.z);
        magnetSpell.setDeltaMovement(lookDirection.scale(0.075F));
        magnetSpell.setMagnetRadius(10.0F);
        magnetSpell.setMaxRange(25);

        level.addFreshEntity(magnetSpell);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z, 15, 0.4, 0.4, 0.4, 0.05);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z, 7, 0.4, 0.4, 0.4, 0.03);

            for (int i = 0; i < 6; i++) {
                double angle = (i * 2.0 * Math.PI) / 6;
                double x = particlePos.x + Math.cos(angle) * 1.2;
                double z = particlePos.z + Math.sin(angle) * 1.2;

                serverLevel.sendParticles(ParticleTypes.ENCHANT, x, particlePos.y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public String getName() {
        return "Magnet Spell";
    }
}