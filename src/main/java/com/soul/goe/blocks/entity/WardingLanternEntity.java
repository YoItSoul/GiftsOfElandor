package com.soul.goe.blocks.entity;

import com.soul.goe.Config;
import com.soul.goe.registry.ModBlockEntities;
import com.soul.goe.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class WardingLanternEntity extends BlockEntity {
    private final AABB searchArea;
    private long lastParticleTime;
    private final Set<EntityType<?>> whitelistedEntities;
    private boolean isLoaded = false;
    private static final int TICK_INTERVAL = 60;
    private int tickCounter = 0;


    public WardingLanternEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDING_LANTERN.get(), pos, state);
        this.searchArea = new AABB(pos).inflate(Config.WARDING_BLOCK_RADIUS.get());
        this.whitelistedEntities = initializeWhitelistedEntities();
        this.lastParticleTime = 0;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        isLoaded = true;
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        isLoaded = false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WardingLanternEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            blockEntity.handleTick(serverLevel, pos);
        }
    }

    private Set<EntityType<?>> initializeWhitelistedEntities() {
        Set<EntityType<?>> entities = new HashSet<>();
        for (String entityId : Config.WARDING_WHITELISTED_ENTITIES.get()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);
            if (resourceLocation != null) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).ifPresent(entities::add);
            }
        }
        return entities;
    }

    private void handleTick(ServerLevel level, BlockPos pos) {
        if (++tickCounter % TICK_INTERVAL != 0) return;

        List<Entity> nearbyEntities = level.getEntities((Entity) null, searchArea,
                entity -> entity instanceof Monster && !isWhitelistedMob(entity));

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                pushBackAndIgniteEntity(livingEntity, pos);
            }
        }

        Optional<ServerPlayer> playerWithCharm = getPlayerHoldingCharm(level);
        if (playerWithCharm.isPresent()) {
            sendParticles(level, pos, playerWithCharm.get(), nearbyEntities);
        }
    }

    private boolean isWhitelistedMob(Entity entity) {
        return whitelistedEntities.contains(entity.getType());
    }

    private static void pushBackAndIgniteEntity(LivingEntity entity, BlockPos pos) {
        Vec3 entityPos = entity.position();
        Vec3 lanternPos = Vec3.atCenterOf(pos);
        Vec3 pushDir = entityPos.subtract(lanternPos).normalize();

        double pushStrength = Config.PUSH_STRENGTH.get();
        entity.setDeltaMovement(
                pushDir.x * pushStrength,
                Math.max(0.2, pushDir.y * pushStrength),
                pushDir.z * pushStrength
        );

        if (Config.IGNITE_ENTITIES.get()) {
            entity.setRemainingFireTicks(Config.FIRE_DURATION.get());
        }

        entity.hasImpulse = true;
    }

    private void sendParticles(ServerLevel level, BlockPos pos, ServerPlayer player, List<Entity> entities) {
        long currentTime = level.getGameTime();
        if (currentTime - lastParticleTime < 5) {
            return;
        }
        lastParticleTime = currentTime;

        Vec3 lanternCenter = Vec3.atCenterOf(pos);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && !isWhitelistedMob(entity)) {
                Vec3 entityPos = entity.position();
                Vec3 direction = entityPos.subtract(lanternCenter).normalize();
                double distance = lanternCenter.distanceTo(entityPos);

                int particlesPerLine = Math.max(5, (int)(distance * 2));
                for (int i = 0; i < particlesPerLine; i++) {
                    double progress = i / (double) particlesPerLine;
                    double x = lanternCenter.x + (entityPos.x - lanternCenter.x) * progress;
                    double y = lanternCenter.y + (entityPos.y - lanternCenter.y) * progress;
                    double z = lanternCenter.z + (entityPos.z - lanternCenter.z) * progress;

                    double spread = 0.1;
                    x += level.getRandom().nextDouble() * spread - spread/2;
                    y += level.getRandom().nextDouble() * spread - spread/2;
                    z += level.getRandom().nextDouble() * spread - spread/2;

                    level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 1,
                            direction.x * 0.1, direction.y * 0.1, direction.z * 0.1,
                            Config.PARTICLE_SPEED.get());
                }

                int particlesInCircle = 16;
                double circleRadius = 1.0;
                double angleStep = 2 * Math.PI / particlesInCircle;
                double baseHeight = entityPos.y + 0.1;

                for (int height = 0; height < 2; height++) {
                    double circleHeight = baseHeight + (height * 1.8);
                    for (int i = 0; i < particlesInCircle; i++) {
                        double angle = i * angleStep;
                        double circleX = entityPos.x + Math.cos(angle) * circleRadius;
                        double circleZ = entityPos.z + Math.sin(angle) * circleRadius;
                        double verticalSpeed = height == 0 ? 0.05 : -0.05;

                        level.sendParticles(ParticleTypes.ENCHANT, circleX, circleHeight, circleZ, 1,
                                0, verticalSpeed, 0, Config.PARTICLE_SPEED.get());
                    }
                }
            }
        }
    }

    private static Optional<ServerPlayer> getPlayerHoldingCharm(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return serverLevel.players().stream()
                .filter(player -> player.getMainHandItem().is(ModItems.ELANDORS_CHARM.get()) ||
                        player.getOffhandItem().is(ModItems.ELANDORS_CHARM.get()))
                .findFirst();
    }
}