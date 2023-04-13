package dev.plytki.baseapi.items;

import dev.plytki.baseapi.inventories.util.ItemBuilder;
import dev.plytki.baseapi.inventories.util.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BaseItem extends ItemStack {

    private final String id;
    private final String texture;
    private final String headDatabaseId;

    private BaseItem(String id, ItemStack itemStack) {
        this(id, itemStack, null, null);
    }
    private BaseItem(String id, ItemStack itemStack, String texture) {
        this(id, itemStack, texture, null);
    }

    private BaseItem(String id, ItemStack itemStack, String texture, String headDatabaseId) {
        super(itemStack);
        this.id = id;
        this.texture = texture;
        this.headDatabaseId = headDatabaseId;
    }

    public ItemStack getItem() {
        return super.clone();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static Builder builder(String id, ItemStack itemStack) {
        return new Builder(id, itemStack);
    }

    public static Builder builder(String id, String texture) {
        return new Builder(id, texture);
    }

    public String getId() {
        return id;
    }

    public String getTexture() {
        return texture;
    }

    public String getHeadDatabaseId() {
        return headDatabaseId;
    }

    public ItemBuilder itemBuilder() {
        return new ItemBuilder(this.clone());
    }

    @Override
    public String toString() {
        return "BaseItem{" +
                "id='" + id + '\'' +
                ", texture='" + texture + '\'' +
                '}';
    }

    public static class Builder {

        private ItemStack itemStack;
        private String id;
        private String texture;
        private String headDatabaseId;

        private Builder(String id, String texture, String headDatabaseId) {
            this.id = id;
            this.texture = texture;
            this.itemStack = new SkullBuilder(texture).getSkull();
            this.headDatabaseId = headDatabaseId;
        }

        private Builder(String id, String texture) {
            this.id = id;
            this.texture = texture;
            this.itemStack = new SkullBuilder(texture).getSkull();
        }

        private Builder(String id, ItemStack itemStack) {
            this.id = id;
            this.itemStack = itemStack;
        }

        private Builder(String id) {
            this(id, new ItemStack(Material.PLAYER_HEAD));
        }

        public Builder setTexture(String texture) {
            this.texture = texture;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setHeadDatabaseId(String id) {
            this.headDatabaseId = id;
            return this;
        }

        public BaseItem build() {
            if (this.texture != null)
                this.itemStack = new SkullBuilder(this.texture).getSkull();
            return new BaseItem(this.id, this.itemStack, this.texture, this.headDatabaseId);
        }

    }

}