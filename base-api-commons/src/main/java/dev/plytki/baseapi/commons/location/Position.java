package dev.plytki.baseapi.commons.location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@AllArgsConstructor
@Data
public class Position {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public BaseLocation toLocation(String world) {
        return new BaseLocation(world,this.x,this.y,this.z,this.yaw,this.pitch);
    }

    public BaseLocation toLocation(World world) {
        return new BaseLocation(world,this.x,this.y,this.z,this.yaw,this.pitch);
    }

    public static Position position(double x, double y, double z, float yaw, float pitch) {
        return new Position(x,y,z,yaw,pitch);
    }

    public static Position fromLocation(Location location) {
        return new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public String toString() {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("0.0", otherSymbols);
        return "Position{" +
                "x=" + df.format(x) +
                ", y=" + df.format(y) +
                ", z=" + df.format(z) +
                ", yaw=" + df.format(yaw) +
                ", pitch=" + df.format(pitch) +
                '}';
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static Position fromJSON(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement parse = JsonParser.parseString(json);
        return gson.fromJson(parse, Position.class);
    }

}