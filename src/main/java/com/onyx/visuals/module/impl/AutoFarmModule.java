package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.Category;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.setting.DoubleSetting;
import com.onyx.visuals.setting.EnumSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.*;

/**
 * AutoFarm — walks like a normal player, mines target ores, fights mobs.
 * Uses real key inputs for movement, direct attack() for combat.
 */
public class AutoFarmModule extends Module {

    public enum TargetBlock {
        DIAMOND("Diamonds", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
        IRON("Iron", Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
        COAL("Coal", Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
        GOLD("Gold", Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE),
        EMERALD("Emerald", Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
        LAPIS("Lapis", Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
        REDSTONE("Redstone", Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
        ALL_ORES("All Ores");

        private final String label;
        private final Block[] blocks;

        TargetBlock(String label, Block... blocks) {
            this.label = label;
            this.blocks = blocks;
        }
        public String getLabel() { return label; }
        public boolean matches(Block block) {
            if (this == ALL_ORES) {
                return block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
                       block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
                       block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
                       block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
                       block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
                       block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
                       block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE;
            }
            for (Block b : blocks) if (b == block) return true;
            return false;
        }
    }

    private final EnumSetting<TargetBlock> target = add(new EnumSetting<>("Target", "What to farm.", TargetBlock.DIAMOND));
    private final DoubleSetting mineRange = add(new DoubleSetting("MineRange", "Scan range for ores.", 16.0, 4.0, 32.0, 1.0));

    private State state = State.SCANNING;
    private BlockPos targetPos;
    private int stuckTimer = 0;
    private Vec3d lastPos = Vec3d.ZERO;
    private final Random random = new Random();
    private final Set<BlockPos> minedPositions = new HashSet<>();

    public AutoFarmModule() {
        super("AutoFarm", "Walks like a player, mines ores, fights mobs.", Category.BOT);
    }

    @Override
    public void onEnable() {
        state = State.SCANNING;
        targetPos = null;
        minedPositions.clear();
    }

    @Override
    public void onDisable() {
        releaseAllKeys();
        state = State.SCANNING;
        targetPos = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // 1. Combat first — find and attack mobs
        HostileEntity mob = findNearestHostile(5.0);
        if (mob != null) {
            equipSword();
            faceEntity(mob);
            mc.player.attack(mob);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }

        // 2. State machine for farming
        switch (state) {
            case SCANNING -> scanTick(player);
            case WALKING  -> walkTick(player);
            case MINING   -> mineTick(player);
            case STUCK    -> stuckTick(player);
        }
    }

    // ── Scanning ─────────────────────────────────────────────

    private void scanTick(ClientPlayerEntity player) {
        BlockPos found = findNearestOre(target.get(), mineRange.getInt());
        if (found != null) {
            targetPos = found;
            state = State.WALKING;
            return;
        }
        wanderTick(player);
    }

    private void wanderTick(ClientPlayerEntity player) {
        setKey(mc.options.forwardKey, true);
        setKey(mc.options.sprintKey, true);

        if (random.nextInt(100) < 5) {
            setKey(mc.options.jumpKey, true);
        } else {
            setKey(mc.options.jumpKey, false);
        }

        if (random.nextInt(30) == 0) {
            float turn = (random.nextFloat() - 0.5f) * 90f;
            player.setYaw(player.getYaw() + turn);
        }

        checkStuck(player);
    }

    // ── Walking ───────────────────────────────────────────────

    private void walkTick(ClientPlayerEntity player) {
        if (targetPos == null) { state = State.SCANNING; return; }

        double dist = player.getBoundingBox().getCenter().distanceTo(Vec3d.ofCenter(targetPos));
        if (dist < 3.0) {
            state = State.MINING;
            releaseAllKeys();
            return;
        }

        faceBlock(targetPos);
        setKey(mc.options.forwardKey, true);
        setKey(mc.options.sprintKey, true);

        // Jump over 1-block obstacles
        BlockPos front = player.getBlockPos().offset(player.getHorizontalFacing());
        if (!mc.world.getBlockState(front).isAir() && mc.world.getBlockState(front.up()).isAir()) {
            setKey(mc.options.jumpKey, true);
        } else {
            setKey(mc.options.jumpKey, false);
        }

        checkStuck(player);
    }

    // ── Mining ───────────────────────────────────────────────

    private void mineTick(ClientPlayerEntity player) {
        if (targetPos == null) { state = State.SCANNING; return; }

        // Already mined?
        if (minedPositions.contains(targetPos)) {
            targetPos = null;
            state = State.SCANNING;
            return;
        }

        // Equip pickaxe for mining
        equipPickaxe();

        faceBlock(targetPos);
        BlockHitResult hit = raycastBlock(targetPos);
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(targetPos)) {
            mc.interactionManager.attackBlock(targetPos, hit.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        // Block broken?
        if (mc.world.getBlockState(targetPos).isAir()) {
            minedPositions.add(targetPos);
            targetPos = null;
            state = State.SCANNING;
        }
    }

    // ── Stuck handling ────────────────────────────────────────

    private void stuckTick(ClientPlayerEntity player) {
        setKey(mc.options.jumpKey, true);
        player.setYaw(player.getYaw() + 90);
        stuckTimer++;
        if (stuckTimer > 20) {
            stuckTimer = 0;
            state = State.SCANNING;
        }
    }

    private void checkStuck(ClientPlayerEntity player) {
        if (player.getBoundingBox().getCenter().distanceTo(lastPos) < 0.5) {
            stuckTimer++;
            if (stuckTimer > 15) {
                stuckTimer = 0;
                state = State.STUCK;
                releaseAllKeys();
            }
        } else {
            stuckTimer = 0;
        }
        lastPos = player.getBoundingBox().getCenter();
    }

    // ── Ore Detection ─────────────────────────────────────────

    private BlockPos findNearestOre(TargetBlock target, int range) {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (minedPositions.contains(pos)) continue;

                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (target.matches(block)) {
                        double dist = playerPos.getSquaredDistance(pos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = pos.toImmutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    // ── Combat ────────────────────────────────────────────────

    private HostileEntity findNearestHostile(double range) {
        HostileEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof HostileEntity)) continue;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist < bestDist && dist < range * range) {
                bestDist = dist;
                best = (HostileEntity) entity;
            }
        }
        return best;
    }

    private void faceEntity(Entity entity) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d target = entity.getBoundingBox().getCenter();
        Vec3d diff = target.subtract(eye);
        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }

    // ── Tool Management ───────────────────────────────────────

    private void equipPickaxe() {
        int bestSlot = -1;
        float bestSpeed = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            String name = stack.getItem().toString().toLowerCase();
            if (name.contains("pickaxe")) {
                float speed = stack.getMiningSpeedMultiplier(net.minecraft.block.Blocks.STONE.getDefaultState());
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private void equipSword() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            String name = stack.getItem().toString().toLowerCase();
            if (name.contains("sword")) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void faceBlock(BlockPos pos) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d diff = Vec3d.ofCenter(pos).subtract(eye);
        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }

    private BlockHitResult raycastBlock(BlockPos pos) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d end = Vec3d.ofCenter(pos);
        return mc.world.raycast(new RaycastContext(
                eye, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));
    }

    private void setKey(net.minecraft.client.option.KeyBinding key, boolean pressed) {
        net.minecraft.client.option.KeyBinding.setKeyPressed(key.getDefaultKey(), pressed);
    }

    private void releaseAllKeys() {
        setKey(mc.options.forwardKey, false);
        setKey(mc.options.backKey, false);
        setKey(mc.options.leftKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.jumpKey, false);
        setKey(mc.options.sprintKey, false);
    }

    private enum State { SCANNING, WALKING, MINING, STUCK }
}