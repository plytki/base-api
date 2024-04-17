package dev.plytki.baseapi.inventories.util.legacy;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LegacyItemBuilder {

    private final ItemStack itemStack;

    /**
     * Create a new LegacyItemBuilder from scratch.
     * @param material The material to create the LegacyItemBuilder with.
     */
    public LegacyItemBuilder(Material material){
        this(material, 1);
    }

    /**
     * Create a new LegacyItemBuilder over an existing itemstack.
     * @param itemStack The itemstack to create the LegacyItemBuilder over.
     */
    public LegacyItemBuilder(ItemStack itemStack){
        this.itemStack = itemStack;
    }

    /**
     * Create a new LegacyItemBuilder from scratch.
     * @param material The material of the item.
     * @param amount The amount of the item.
     */
    public LegacyItemBuilder(Material material, int amount){
        this.itemStack = new ItemStack(material, amount);
    }

    /**
     * Create a new LegacyItemBuilder from scratch.
     * @param material The material of the item.
     * @param amount The amount of the item.
     * @param durability The durability of the item.
     */
    public LegacyItemBuilder(Material material, int amount, byte durability){
        this.itemStack = new ItemStack(material, amount, durability);
    }

    /**
     * Clone the LegacyItemBuilder into a new one.
     * @return The cloned instance.
     */
    public LegacyItemBuilder clone(){
        return new LegacyItemBuilder(itemStack.clone());
    }

    /**
     * Change the durability of the item.
     * @param dur The durability to set it to.
     */
    public LegacyItemBuilder setDurability(short dur){
        itemStack.setDurability(dur);
        return this;
    }

    public int getLoreLines() {
        if (itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) return 0;
        return itemStack.getItemMeta().getLore().size();
    }

    /**
     * Set the displayname of the item.
     * @param name The name to change it to.
     */
    public LegacyItemBuilder setName(String name){
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(name);
        itemStack.setItemMeta(im);
        return this;
    }

    public String getName(){
        return itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : getFriendlyName(itemStack.getType());
    }

    /**
     * Set the amount of the item.
     * @param amount The amount.
     */
    public LegacyItemBuilder setAmount(int amount){
        itemStack.setAmount(amount);
        return this;
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    /**
     * Add an unsafe enchantment.
     * @param ench The enchantment to add.
     * @param level The level to put the enchant on.
     */
    public LegacyItemBuilder addUnsafeEnchantment(Enchantment ench, int level){
        itemStack.addUnsafeEnchantment(ench, level);
        return this;
    }

    /**
     * Remove a certain enchant from the item.
     * @param ench The enchantment to remove
     */
    public LegacyItemBuilder removeEnchantment(Enchantment ench){
        itemStack.removeEnchantment(ench);
        return this;
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     * @param owner The name of the skull's owner.
     */
    public LegacyItemBuilder setSkullOwner(String owner){
        try{
            SkullMeta im = (SkullMeta) itemStack.getItemMeta();
            im.setOwner(owner);
            itemStack.setItemMeta(im);
        }catch(ClassCastException expected){}
        return this;
    }

    /**
     * Add an enchant to the item.
     * @param ench The enchant to add
     * @param level The level
     */
    public LegacyItemBuilder addEnchant(Enchantment ench, int level){
        ItemMeta im = itemStack.getItemMeta();
        im.addEnchant(ench, level, true);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Add multiple enchants at once.
     * @param enchantments The enchants to add.
     */
    public LegacyItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments){
        itemStack.addEnchantments(enchantments);
        return this;
    }

    /**
     * Sets infinity durability on the item by setting the durability to Short.MAX_VALUE.
     */
    public LegacyItemBuilder setInfinityDurability(){
        itemStack.setDurability(Short.MAX_VALUE);
        return this;
    }

    /**
     * Re-sets the lore.
     * @param lore The lore to set it to.
     */
    public LegacyItemBuilder setLore(String... lore){
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Re-sets the lore.
     * @param lore The lore to set it to.
     */
    public LegacyItemBuilder setLore(List<String> lore) {
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     */
    public LegacyItemBuilder removeLoreLine(String line){
        ItemMeta im = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if(!lore.contains(line))return this;
        lore.remove(line);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     * @param index The index of the lore line to remove.
     */
    public LegacyItemBuilder removeLoreLine(int index){
        ItemMeta im = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if (index<0 || index > lore.size())
            return this;
        lore.remove(index);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     * @param line The lore line to add.
     */
    public LegacyItemBuilder addLoreLine(String line){
        ItemMeta im = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();
        if(im.hasLore())lore = new ArrayList<>(im.getLore());
        lore.add(line);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     * @param line The lore line to add.
     * @param pos The index of where to put it.
     */
    public LegacyItemBuilder addLoreLine(String line, int pos){
        ItemMeta im = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        lore.set(pos, line);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     * @param color The color to set it to.
     */
    public LegacyItemBuilder setLeatherArmorColor(Color color){
        try {
            LeatherArmorMeta im = (LeatherArmorMeta) itemStack.getItemMeta();
            im.setColor(color);
            itemStack.setItemMeta(im);
        } catch (ClassCastException ignored) {}
        return this;
    }

    /**
     * Retrieves the itemstack from the LegacyItemBuilder.
     * @return The itemstack created/modified by the LegacyItemBuilder instance.
     */

    public ItemStack toItemStack() {
        return itemStack;
    }

    public LegacyItemBuilder addItemFlag(ItemFlag itemFlag) {
        ItemMeta im = itemStack.getItemMeta();
        im.addItemFlags(itemFlag);
        itemStack.setItemMeta(im);
        return this;
    }

    public LegacyItemBuilder addItemFlags(ItemFlag... itemFlags) {
        ItemMeta im = itemStack.getItemMeta();
        im.addItemFlags(itemFlags);
        itemStack.setItemMeta(im);
        return this;
    }

    private String getFriendlyName(Material material) {
        return Arrays.stream(material.toString().split("_"))
                .map(str -> str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

}