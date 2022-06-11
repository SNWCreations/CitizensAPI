package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Squid;
import org.bukkit.entity.WaterMob;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

public class SwimmingExaminer implements BlockExaminer {
    private boolean canSwimInLava;
    private final NPC npc;

    public SwimmingExaminer(NPC npc) {
        this.npc = npc;
    }

    public boolean canSwimInLava() {
        return canSwimInLava;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        // penalise non water blocks for fish
        if (isWaterMob(npc.getEntity())) {
            Vector vector = point.getVector();
            if (!MinecraftBlockExaminer.isLiquidOrInLiquid(
                    source.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()))) {
                return 1F;
            }
        }
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector vector = point.getVector();
        if (!MinecraftBlockExaminer.isLiquidOrInLiquid(
                source.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()))) {
            return PassableState.IGNORE;
        }
        if (isWaterMob(npc.getEntity())) {
            return PassableState.PASSABLE;
        }
        Material above = source.getMaterialAt(vector.clone().add(UP));
        return isSwimmableLiquid(above) || MinecraftBlockExaminer.canStandIn(above) ? PassableState.PASSABLE
                : PassableState.UNPASSABLE;
    }

    private boolean isSwimmableLiquid(Material material) {
        if (material == Material.LAVA
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_LAVA")))
            return canSwimInLava();
        return material == Material.WATER
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_WATER"));
    }

    public boolean isSwimmingUp() {
        return npc.data().has(NPC.Metadata.SWIMMING) ? npc.data().<Boolean> get(NPC.Metadata.SWIMMING)
                : !isWaterMob(npc.getEntity());
    }

    public void setCanSwimInLava(boolean canSwimInLava) {
        this.canSwimInLava = canSwimInLava;
    }

    public static boolean isWaterMob(Entity entity) {
        if (!SpigotUtil.isUsing1_13API())
            return entity instanceof Squid;
        return entity instanceof WaterMob || entity.getType().name().equals("TURTLE")
                || entity.getType().name().equals("FROG") || entity.getType().name().equals("AXOLOTL");
    }

    private static final Vector UP = new Vector(0, 1, 0);
}
