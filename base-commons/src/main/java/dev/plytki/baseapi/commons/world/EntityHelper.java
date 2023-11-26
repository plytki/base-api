package dev.plytki.baseapi.commons.world;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class EntityHelper {

    public static List<Player> getPlayersInRadius(Location location, double size) {
        return getEntitiesInRadius(location, size, Player.class);
    }

    public static List<Player> getPlayersInRadius(Location location, double size, double padding) {
        return getEntitiesInRadius(location, size, padding, Player.class);
    }

    public static <T extends Entity> List<T> getEntitiesInRadius(Location location, double size, Class<T> entityType) {
        return getEntitiesInRadius(location, size, 0, entityType);
    }

    public static <T extends Entity> List<T> getEntitiesInRadius(Location location, double size, double padding, Class<T> entityType) {
        return location
                .getWorld()
                .getEntities()
                .stream()
                .filter(entityType::isInstance)
                .filter(entity -> location.distance(entity.getLocation()) <= size && location.distance(entity.getLocation()) >= padding)
                .map(entityType::cast)
                .collect(Collectors.toList());
    }

}