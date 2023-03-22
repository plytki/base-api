package dev.plytki.baseapi.commons.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BaseLocation extends Location {

    public BaseLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
    public BaseLocation(String world, double x, double y, double z) {
        super(Bukkit.getWorld(world), x, y, z);
    }

    public BaseLocation(String world, double x, double y, double z, float yaw, float pitch) {
        super(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public BaseLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public BaseLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    /**
     * @return A new location where X/Y/Z are on the Block location (integer value of X/Y/Z)
     */
    @NotNull
    public Location toBlockLocation() {
        Location blockLoc = clone();
        blockLoc.setX(getBlockX());
        blockLoc.setY(getBlockY());
        blockLoc.setZ(getBlockZ());
        return blockLoc;
    }

    /**
     * @return A new location where X/Y/Z are the center of the block
     */
    @NotNull
    public Location toCenterLocation() {
        Location centerLoc = clone();
        centerLoc.setX(getBlockX() + 0.5);
        centerLoc.setY(getBlockY() + 0.5);
        centerLoc.setZ(getBlockZ() + 0.5);
        return centerLoc;
    }

    @NotNull
    public Collection<Entity> getNearbyEntities(double x, double y, double z) {
        World world = this.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world.getNearbyEntities(this, x, y, z);
    }

    /**
     * Creates explosion at this location with given power
     *
     * Will break blocks and ignite blocks on fire.
     *
     * @param power The power of explosion, where 4F is TNT
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(float power) {
        return this.getWorld().createExplosion(this, power);
    }

    /**
     * Creates explosion at this location with given power and optionally
     * setting blocks on fire.
     *
     * Will break blocks.
     *
     * @param power The power of explosion, where 4F is TNT
     * @param setFire Whether or not to set blocks on fire
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(float power, boolean setFire) {
        return this.getWorld().createExplosion(this, power, setFire);
    }

    public BaseLocation set(double x, double y, double z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        return this;
    }

    @Override
    public BaseLocation clone() {
        BaseLocation clone = (BaseLocation) super.clone();
        return new BaseLocation(clone);
    }

}
