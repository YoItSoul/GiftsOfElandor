package com.soul.goe.spells.entities.projectiles;

import com.soul.goe.spells.config.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class MiningForceEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Integer> MINING_SIZE = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(MiningForceEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int DEFAULT_MINING_SIZE = 1;
    private static final int DEFAULT_MAX_RANGE = 80;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final int PARTICLE_INTERVAL = 1;
    private static final double TRAIL_PARTICLE_SPREAD = 0.1;
    private static final int EXPLOSION_PARTICLE_COUNT = 20;
    private static final double EXPLOSION_PARTICLE_SPREAD = 0.4;
    private static final float EXPLOSION_VOLUME = 0.8F;
    private static final float EXPLOSION_PITCH = 1.1F;

    private int ticksInAir = 0;
    private Vec3 startPos;
    private Vec3 lastDirection;

    public MiningForceEntity(EntityType<? extends MiningForceEntity> entityType, Level level) {
        super(entityType, level);
    }

    public MiningForceEntity(EntityType<? extends MiningForceEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setMiningSize(DEFAULT_MINING_SIZE);
        setMaxRange(DEFAULT_MAX_RANGE);
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setIsCriticalCast(false);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.startPos = owner.position();
        this.lastDirection = owner.getLookAngle();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MINING_SIZE, DEFAULT_MINING_SIZE);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
    }

    @Override
    public void setDamage(float damage) {
    }

    @Override
    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    public void setMiningSize(int size) {
        this.entityData.set(MINING_SIZE, size);
    }

    public int getMiningSize() {
        return this.entityData.get(MINING_SIZE);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    @Override
    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    @Override
    public void setMaxRange(int range) {
        this.entityData.set(MAX_RANGE, range);
    }

    public int getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }

    @Override
    public void tick() {
        super.tick();
        ticksInAir++;

        Vec3 motion = getDeltaMovement();
        this.lastDirection = motion.normalize();

        if (startPos != null && distanceToSqr(startPos) > getMaxRange() * getMaxRange()) {
            explode();
            return;
        }

        if (this.level().isClientSide()) {
            spawnTrailParticles();
        } else {
            mineBlocksAroundProjectile();

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                onHit(hitResult);
                return;
            }
        }

        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
    }

    private void spawnTrailParticles() {
        if (isCriticalCast()) {
            level().addParticle(ParticleTypes.ENCHANT,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);

            level().addParticle(ParticleTypes.CRIT,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                    0, 0, 0);
        } else {
            level().addParticle(ParticleTypes.CRIT,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
    }

    private void mineBlocksAroundProjectile() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player)) return;

        ItemStack pickaxe = findPickaxeInInventory(player);
        if (pickaxe.isEmpty() || pickaxe.getDamageValue() >= pickaxe.getMaxDamage() - 1) {
            explode();
            return;
        }

        BlockPos centerPos = blockPosition();
        int miningSize = getMiningSize();
        int radius = miningSize / 2;

        int minedBlocks = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    if (mineBlock(pos, player, pickaxe)) {
                        minedBlocks++;
                        if (pickaxe.isEmpty() || pickaxe.getDamageValue() >= pickaxe.getMaxDamage() - 1) {
                            explode();
                            return;
                        }
                    }
                }
            }
        }

        if (minedBlocks > 0) {
            spawnMiningEffects(centerPos, minedBlocks);
        }
    }

    private ItemStack findPickaxeInInventory(Player player) {
        ItemStack bestPickaxe = ItemStack.EMPTY;
        int bestDurability = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PickaxeItem) {
                int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                if (remainingDurability > bestDurability) {
                    bestDurability = remainingDurability;
                    bestPickaxe = stack;
                }
            }
        }

        return bestPickaxe;
    }

    public static boolean hasUsablePickaxe(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PickaxeItem) {
                int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                if (remainingDurability > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean mineBlock(BlockPos pos, Player player, ItemStack pickaxe) {
        BlockState state = level().getBlockState(pos);

        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return false;
        }

        try {
            Block.dropResources(state, level(), pos, level().getBlockEntity(pos), player, pickaxe);
            boolean destroyed = level().destroyBlock(pos, false);

            if (destroyed) {
                pickaxe.hurtAndBreak(1, player, player.getEquipmentSlotForItem(pickaxe));
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private void spawnMiningEffects(BlockPos center, int minedBlocks) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    minedBlocks * 3,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    minedBlocks * 2,
                    0.3, 0.3, 0.3, 0.05);

            level().playSound(null, center.getX(), center.getY(), center.getZ(),
                    SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS,
                    0.8F, 1.0F);
        }
    }

    private void explode() {
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.1);

                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT / 2,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.05);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}