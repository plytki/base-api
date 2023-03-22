package dev.plytki.baseapi.commons.location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldPosition extends Position {

    private final String world;

    public WorldPosition(String world, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);
        this.world = world;
    }

    public BaseLocation toLocation() {
        return new BaseLocation(this.world, this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
    }

    public static WorldPosition position(String world, double x, double y, double z, float yaw, float pitch) {
        return new WorldPosition(world,x,y,z,yaw,pitch);
    }

    public World getWorld() {
         return Bukkit.getWorld(this.world);
    }

    public static WorldPosition fromLocation(Location location) {
        return new WorldPosition(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static WorldPosition fromJSON(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement parse = JsonParser.parseString(json);
        return gson.fromJson(parse, WorldPosition.class);
    }

}