package com.kihz.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kihz.Core;
import com.kihz.events.ItemGenerateEvent;
import com.kihz.utils.NMSUtils;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.serializers.bukkit.ItemStackSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
public abstract class ItemWrapper {
    private ItemStack item;
    private int amount = 1;
    private ItemMeta meta;
    private CompoundTag tag;
    private CompoundTag originalTag;
    private ItemType type;
    protected boolean needsRegen;
    private boolean preventRegen;
    private boolean destroyed;
    @Setter private String obtainableLore = ""; // Each obtainable from must be split up using a ','
    @Setter private boolean antiDupe;
    @Setter private boolean enchanted;
    @Setter private boolean alwaysCancel;
    @Setter private boolean preventStorage;

    protected boolean overrideCustomName = true;

    public static final String TYPE_TAG = "type";
    public static final String DUPE_TAG = "dupe";
    public static final String NAME_TAG = "customName";
    public static final String LORE_TAG = "customLore";
    public static final String OBTAINABLE_TAG = "obtainableLore";
    public static final String ORIGIN_TAG = "origin"; // Tells us where this item came from
    public static final String SERVER_TAG = "server"; // Tells us what server this item was generated on
    public static final String DROP_CHANCE = "dropChance";
    public static final String ENCHANTMENTS_TAG = "Enchantments";

    public static final Enchantment ENCHANT = Enchantment.values()[0]; // This will be our vanilla enchant so that we can make items glow (enchant flags are hidden, so we wont see the enchant lore)

    public ItemWrapper(ItemStack item) {
        this(item, false);
    }

    public ItemWrapper(ItemStack item, boolean preventRegen) {
        this.item = item;
        this.amount = item != null ? item.getAmount() : 1;
        this.meta = item != null ? item.getItemMeta() : null;
        this.originalTag = getTag().copy();
        this.type = getEnum(TYPE_TAG, ItemType.class);
        this.needsRegen = true; // Fully regenerate item, this will prevent lore from appearing on the item twice if there are no changes to the item.
        this.preventRegen = preventRegen;
        this.antiDupe = hasKey(DUPE_TAG);
        this.enchanted = item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(ENCHANT);
        addObtainableLore();
    }

    public ItemWrapper(ItemType type) {
        this.type = type;
        this.originalTag = createNewNBTTag();
        addObtainableLore();
    }

    public ItemWrapper(CompoundTag tag) {
        this.originalTag = tag;
        this.tag = tag;
        this.type = getEnum(TYPE_TAG, ItemType.class);
        this.antiDupe = hasKey(DUPE_TAG);
        this.needsRegen = true;
        this.amount = 1;
    }

    /**
     * Returns the raw vanilla ItemStack;
     * @return rawStack
     */
    public abstract ItemStack getRawStack();

    /**
     * Applies changes to the item.
     */
    public abstract void updateItem();

    /**
     * Add obtainable lore to this item
     */
    protected void addObtainableLore() {

    }

    /**
     * Generate this item into an ItemStack.
     * @return item
     */
    public ItemStack generateItem() {
        return generateItem(true);
    }

    /**
     * Generate this item into an ItemStack.
     * @return item
     */
    public ItemStack generateItem(boolean shouldCallEvent) {
        if(isDestroyed() || (isPreventRegen() && !Utils.isAir(getItem()))) { // Don't generate a new item if we are destroyed or we aren't supposed to
            getItem().setAmount(getAmount());
            return getItem();
        }

        this.item = getRawStack(); // Creates a new ItemStack with none of our custom data

        updateItem(); // Apply our custom data
        getItem().setAmount(getAmount());
        generateNewDupeTag(false); // Apply the dupe tag
        setEnum(TYPE_TAG, getType()); // Apply our itemType

        setTagString(OBTAINABLE_TAG, obtainableLore);
        if(hasKey(NAME_TAG) && overrideCustomName) // Set our display name if we have one
            setDisplayName(getCustomName());
        if(hasKey(LORE_TAG)) // Set the lore if we have it
            addLore(ChatColor.ITALIC + getTagString(LORE_TAG));

        if(shouldCallEvent)
            Utils.runSynchronous(() -> Bukkit.getPluginManager().callEvent(new ItemGenerateEvent(this)));

        updateNBTTag();
        this.needsRegen = true;

        ItemStack itemStack = getItem();
        ItemMeta finalMeta = itemStack.getItemMeta();

        if(isEnchanted()) { // Set the item to glow and hide the enchant flags
            finalMeta.addEnchant(ENCHANT, 1, true);
        } else {
            finalMeta.removeEnchant(ENCHANT);
            finalMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        finalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Apply our meta and return the item
        itemStack.setItemMeta(finalMeta);
        return itemStack;
    }

    /**
     * Combine our ItemMeta with our CompoundTag.
     */
    protected void updateNBTTag() {
        ItemStack itemWithMeta = getItem().clone();
        itemWithMeta.setItemMeta(getMeta());
        net.minecraft.world.item.ItemStack nmsItem = NMSUtils.getNMSItem(itemWithMeta);

        if(nmsItem != null && nmsItem.getTag() != null) { // There is some data that we need to merge with our NBTTag
            CompoundTag nmsTag = nmsItem.getTag();
            for(String key : nmsTag.getAllKeys())
                if(getTag().contains(key) == getOriginalTag().contains(key) && (!getTag().contains(key) || getTag().get(key).equals(getOriginalTag().get(key))))
                    getTag().put(key, nmsTag.get(key)); // Load from ItemMeta if we have not changed or removed the value manually during re-generation.
        }

        this.item = NMSUtils.getUnsafe().modifyItemStack(getItem(), getTag().toString());
    }

    /**
     * Get this item's meta.
     * @return meta
     */
    public ItemMeta getMeta() {
        if(this.meta == null)
            this.meta = getItem() != null ? getItem().getItemMeta() : getRawStack().getItemMeta();
        return this.meta;
    }

    /**
     * Generate a new dupe tag.
     * @param forceNew Should we override the existing dupe tag?
     * @return dupeTag
     */
    public String generateNewDupeTag(boolean forceNew) {
        if(forceNew || (isAntiDupe() && !hasKey(DUPE_TAG))) {
            String typeName = getItem() != null ? getItem().getType().name() : getType().name();
            setTagString(DUPE_TAG, Utils.nextInt(Integer.MAX_VALUE) + typeName + System.currentTimeMillis());
        }
        return getTagString(DUPE_TAG);
    }

    /**
     * Generate a given amount of this item
     * @param amount The amount ot generate
     * @return item
     */
    public ItemStack generateAmount(int amount) {
        setAmount(amount);
        return generateItem();
    }

    /**
     * Does the NBTTag contain this key?
     * @param key The key to check
     * @return hasKey
     */
    public boolean hasKey(String key) {
        return getTag().contains(key);
    }

    public CompoundTag getTag() {
        if(this.tag == null) { // If we don't have a tag, get it from NMS and clone it
            this.tag = getNBTTag(getItem());
            if(this.tag != null)
                this.tag = this.tag.copy();
        }

        if(this.tag == null) // The NMS Item didn't have an NBTTag so create one ourselves
            this.tag = createNewNBTTag();

        return this.tag;
    }

    /**
     * Get an CompoundTag from the base tag. Will create a new tag if it doesn't exist.
     * @param key The ke to index with
     * @return tag
     */
    public CompoundTag getTag(String key) {
        if(!hasKey(key))
            getTag().put(key, new CompoundTag());
        return getTag().getCompound(key);
    }

    /**
     * Apply a base NBT value to this item's NBTTag.
     * @param key The key to index with
     * @param base The value to set
     */
    public void setTag(String key, Tag base) {
        remove(key);
        if(base != null)
            getTag().put(key, base);
    }

    /**
     * Get a string from NBTTag. Returns null if the key isn't found.
     * @param key The key to index by
     * @return valueAsString
     */
    public String getTagString(String key) {
        return hasKey(key) ? getTag().getString(key) : null;
    }

    /**
     * Clone this wrapper
     * @return cloned
     */
    public ItemWrapper clone() {
        return ItemManager.constructItem(generateItem());
    }

    /**
     * Apply a String value to this item's NBTTag.
     * @param key The key to index with
     * @param value The value to set
     */
    public void setTagString(String key, String value) {
        remove(key);
        if(value != null)
            getTag().putString(key, value);
    }

    /**
     * Get a boolean from NBTTag
     * @param key The key to index by
     * @return valueAsBoolean
     */
    public boolean getTagBoolean(String key) {
        return getTag().getBoolean(key);
    }

    /**
     * Apply a boolean value to this item's NBTTag.
     * @param key The key to index with
     * @param b The value to set
     */
    public void setTagBoolean(String key, boolean b) {
        remove(key);
        if(b)
            getTag().putBoolean(key, true);
    }

    /**
     * Returns an enum from NBTTag. Returns null if the key isn't found.
     * @param key The key to index by
     * @param enumClass The enum class
     * @return valueAsEnum
     */
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
        return hasKey(key) ? Utils.getEnum(getTagString(key), enumClass) : null;
    }

    /**
     * Apply an Enum value to this item's NBTTag.
     * @param key The key to index with
     * @param value The value to set
     */
    public void setEnum(String key, Enum<?> value) {
        remove(key);
        if(value != null)
            setTagString(key, value.name());
    }

    /**
     * Get an int from NBTTag
     * @param key The key to index by
     * @return valueAsInt
     */
    public int getTagInt(String key) {
        return getTag().getInt(key);
    }

    /**
     * Apply an int value to this item's NBTTag.
     * @param key The key to index with
     * @param value The value to set
     */
    public void setTagInt(String key, int value) {
        remove(key);
        if(value != 0)
            getTag().putInt(key, value);
    }

    /**
     * Get a double from NBTTag
     * @param key The key to index by
     * @return valueAsDouble
     */
    public double getTagDouble(String key) {
        return getTag().getDouble(key);
    }

    /**
     * Apply a double value to this item's NBTTag.
     * @param key The key to index with
     * @param value The value to set
     */
    public void setTagDouble(String key, double value) {
        remove(key);
        if(value != 0)
            getTag().putDouble(key, value);
    }

    /**
     * Get a long from NBTTag.
     * @param key The key to index by
     * @return valueAsLong
     */
    public long getTagLong(String key) {
        return getTag().getLong(key);
    }

    /**
     * Apply a long value to this item's NBTTag.
     * @param key The key to index with
     * @param value The value to set
     */
    public void setTagLong(String key, long value) {
        remove(key);
        if(value != 0)
            getTag().putLong(key, value);
    }

    /**
     * Removes this item's NBTTag by its key.
     * @param key The key to remove
     * @return hadKey
     */
    public boolean remove(String key) {
        boolean hasKey = hasKey(key);
        getTag().remove(key);
        return hasKey;
    }

    /**
     * Removes this item's NBTTag by its key and return the removed value.
     * @param key The key to remove
     * @return removedTag
     */
    public CompoundTag removeTag(String key) {
        CompoundTag value = getTag(key);
        remove(key);
        return value;
    }

    /**
     * Returns our custom overridden name if we have one.
     * @return overriddenName
     */
    public String getOverriddenName() {
        return getTagString(NAME_TAG);
    }

    /**
     * Get this custom name of this item.
     * @return customName
     */
    public String getCustomName() {
        return getOverriddenName();
    }

    /**
     * Set the item's name. This will persist.
     * This has a very specific use, avoid using this.
     * @param newName The name to set
     */
    public void setCustomName(String newName) {
        setTagString(NAME_TAG, newName);
    }

    /**
     * Return the display name of this item.
     * @return displayName
     */
    public Component getDisplayName() {
        return getMeta().displayName();
    }

    public String getDisplayNameAsString() {
        return ((TextComponent)getMeta().displayName()).content();
    }

    /**
     * Set the display name for this item.
     * @param newName The new name to set.
     * @return this
     */
    public ItemWrapper setDisplayName(String newName) {
        getMeta().displayName(Component.text(newName, NamedTextColor.WHITE));
        return this;
    }

    /**
     * Set the display name for this item.
     * @param component The new name to set.
     * @return this
     */
    public ItemWrapper setDisplayName(Component component) {
        getMeta().displayName(component);
        return this;
    }

    /**
     * Get the lore on this item
     * @return lore
     */
    public List<Component> getLore() {
        return getMeta().hasLore() ? getMeta().lore() : new ArrayList<>();
    }

    /**
     * Add lore to this item
     * @param lore The lines of lore to add
     * @return this
     */
    public ItemWrapper addLore(String... lore) {
        List<Component> loreList = getLore();

        if(needsRegen) {
            loreList.clear();
            needsRegen = false;
        }

        for(String l : lore)
            loreList.add(Component.text(l, NamedTextColor.GRAY));

        getMeta().lore(loreList);
        return this;
    }

    /**
     * Add lore to this item
     * @param lore The lines of lore to add
     * @return this
     */
    public ItemWrapper addLore(List<String> lore) {
        return addLore(lore.toArray(new String[0]));
    }

    /**
     * Clear this item's lore
     * @return this
     */
    public ItemWrapper clearLore() {
        getMeta().lore(new ArrayList<>());
        return this;
    }

    /**
     * Set this item's lore to a given list of strings
     * @param lore The lore to set
     * @return this
     */
    public ItemWrapper setLore(List<Component> lore) {
        getMeta().lore(lore);
        return this;
    }

    /**
     * Set the stack size for the generated ItemStack
     * @param amount The amount of items in the ItemStack
     * @return this
     */
    public ItemWrapper setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Consume a given stack size from the wrapper's ItemStack. Will destroy the wrapper if the new amount is 0.
     * Returns false if there amountToRemove > amount
     * @param amountToRemove
     * @return
     */
    public boolean consume(int amountToRemove) {
        if(amountToRemove > getAmount())
            return false;

        setAmount(getAmount() - amountToRemove);
        if(getAmount() <= 0)
            destroy();

        return true;
    }

    /**
     * Destroy this item. Will set the item to AIR the next time it's generated if destroyed = true
     */
    public void destroy() {
        this.destroyed = true;
    }

    /**
     * Flag this item as not destroyed.
     */
    public void undestroy() {
        setAmount(Math.max(1, getAmount()));
        this.destroyed = false;
    }

    /**
     * Add a vanilla enchant to this item
     * @param enchantment The enchant to add
     * @param level The level of the enchant
     */
    public void addVanillaEnchant(Enchantment enchantment, int level) {
        ItemMeta meta = getMeta();
        meta.addEnchant(enchantment, level, true);
        this.meta = meta;
    }

    /**
     * Get the color for the item to glow when it is dropped
     * @return glowColor
     */
    public ChatColor getGlowColor() {
        return null;
    }

    /**
     * Returns the state of this ItemFlag.
     * @param tag The ItemTag to check
     * @return isFlag
     */
    public boolean isFlag(ItemTag tag) {
        return getTagBoolean(tag.getNBTTag());
    }

    /**
     * Apply the state of an ItemFlag to this item.
     * @param tag The tag to apply
     * @param state The state to set
     */
    public void setFlag(ItemTag tag, boolean state) {
        setTagBoolean(tag.getNBTTag(), state);
    }

    /**
     * Is this item soulbound?
     * @return isSoulbound
     */
    public boolean isSoulbound() {
        return isFlag(ItemTag.SOULBOUND);
    }

    /**
     * Is this item untradeable?
     * @return isUntradeable
     */
    public boolean isUntradeable() {
        return isFlag(ItemTag.UNTRADEABLE);
    }

    /**
     * Set if this item is soulbound.
     * @param newState If this is a soulbound item.
     */
    public ItemWrapper setSoulbound(boolean newState) {
        setFlag(ItemTag.SOULBOUND, newState);
        return this;
    }

    /**
     * Set if this item is untradeable.
     * @param newState If this is an untradeable item.
     */
    public ItemWrapper setUntradeable(boolean newState) {
        setFlag(ItemTag.UNTRADEABLE, newState);
        return this;
    }

    /**
     * Add a tag on the item about where it was created from.
     * @param desc The description of where the item was created.
     * @return this
     */
    public ItemWrapper logCreation(String desc) {
        if(!isAntiDupe() || hasKey(ORIGIN_TAG))
            return this;
        setTagString(ORIGIN_TAG, desc);
        return this;
    }

    /**
     * Remove tags that we don't need inside of GUIs
     * @param item The item to remove tags from
     * @return item
     */
    public static ItemStack removeCheckTags(org.bukkit.inventory.ItemStack item) {
        return removeTag(item, DUPE_TAG, ORIGIN_TAG, SERVER_TAG, OBTAINABLE_TAG);
    }

    /**
     * Generate a new CompoundTag.
     * @return NBTTag
     */
    public static CompoundTag createNewNBTTag() {
        return new CompoundTag();
    }

    /**
     * Returns a bukkit item's NBTTag. Will return null if there is no tag.
     * @param item The item to get the NBTTag of
     * @return nbtTag
     */
    public static CompoundTag getNBTTag(ItemStack item) {
        return !Utils.isAir(item) ? NMSUtils.getNMSItem(item).getTag() : null;
    }

    /**
     * Generate a new blank nbt tag compound.
     * @return newTag
     */
    public static CompoundTag newNBTTag() {
        return new CompoundTag();
    }

    /**
     * Apply a new NBTTag to a bukkit ItemStack.
     * @param item The item stack to apply the NBTTag to
     * @param tag The NBTTag to apply
     * @return item
     */
    public static ItemStack setNBTTag(ItemStack item, CompoundTag tag) {
        if(Utils.isAir(item))
            return item;
        net.minecraft.world.item.ItemStack nmsItem = NMSUtils.asNMSCopy(item);
        nmsItem.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    /**
     * Is the given item a specific ItemType?
     * @param item The item t o check
     * @param type The item type to check
     * @return isType
     */
    public static boolean isType(ItemStack item, ItemType type) {
        CompoundTag tag = getNBTTag(item);
        return tag != null && type.name().equals(tag.getString(TYPE_TAG));
    }

    /**
     * Is the given item a specific ItemType?
     * @param item The item t o check
     * @param types The item types to check
     * @return isType
     */
    public static boolean isType(ItemStack item, ItemType... types) {
        CompoundTag tag = getNBTTag(item);
        if(tag == null || !tag.contains(TYPE_TAG)) // We don't have a Type tag applied
            return false;

        String itemTypeString = tag.getString(TYPE_TAG);
        for(ItemType type : types) { // Check if we have a matching item type
            if (type.name().equals(itemTypeString))
                return true;
        }

        return false;
    }

    /**
     * Add a lore action.
     * @param click The mouse click type that fires this action.
     * @param action The action to display.
     * @return this
     */
    public ItemWrapper addLoreAction(String click, String action) {
        String control = ChatColor.WHITE + click + "-Click: " + ChatColor.GRAY;
        for (Component component : getLore())
            if(((TextComponent)component).content().startsWith(ChatColor.GRAY + control))
                return this;
        return addLore(control + action);
    }

    /**
     * Remove an NBT tag from this item
     * @param item The item to remove from
     * @param tags The tags to remove
     * @return item
     */
    public static ItemStack removeTag(ItemStack item, String... tags) {
        if (Utils.isAir(item))
            return new ItemStack(Material.AIR);
        net.minecraft.world.item.ItemStack nms = NMSUtils.asNMSCopy(item);
        if (nms.getTag() != null)
            for (String tag : tags)
                nms.getTag().remove(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }

    /**
     * Get our custom ItemType from a bukkit ItemStack. Returns null if an ItemType isn't found.
     * @param item The itemstack to get the type for
     * @return itemType
     */
    public static ItemType getType(ItemStack item) {
        CompoundTag tag = getNBTTag(item);
        if(tag == null || !tag.contains(TYPE_TAG)) // We don't have an NBTTag OR we haven't been assigned an ItemType
            return null;

        String typeString = tag.getString(TYPE_TAG);
        for(ItemType type : ItemType.values()) { // Check for a matching ItemType
            if(type.name().equals(typeString))
                return type;
        }

        return null;
    }

    /**
     * Return a given item's unique dupe id.
     * Returns null when the item doesn't have a dupe tag.
     * @param item The item to get the dupe tag for.
     * @return dupeId
     */
    public static String getDupeId(ItemStack item) {
        CompoundTag nbt = getNBTTag(item);
        return nbt != null && nbt.contains(DUPE_TAG) ? nbt.getString(DUPE_TAG) : null;
    }

    /**
     * Load an NBTTag from a given string.
     * @param nbtJson The NBT data as JSON
     * @return nbtData
     */
    public static CompoundTag parseNBT(JsonObject nbtJson) {
//        String tag = "{a:" + nbt + "}"; // Loads a field, we do it in this jank way because Mojang's parsing methods are private and it isn't worth using reflection
//        try {
//             return (T) MojangsonParser.parse(tag).get("a");
//        } catch (CommandSyntaxException e) {
//            throw new GeneralException("Failed to parse NBTTag for " + nbt, e);
//        }

        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : nbtJson.entrySet()) {
            String key = entry.getKey();
            if(key.equals(ItemStackSerializer.DISPLAY_KEY)) {
                JsonObject displayObj = entry.getValue().getAsJsonObject();
                CompoundTag displayCompound = new CompoundTag();

                // Add name to tag
                if(displayObj.has(ItemStackSerializer.NAME_KEY))
                    displayCompound.put(ItemStackSerializer.NAME_KEY, StringTag.valueOf(displayObj.get(ItemStackSerializer.NAME_KEY).toString()));

                // Add lore to tag
                ListTag tagList = new ListTag();
                JsonElement loreElement = displayObj.get(ItemStackSerializer.LORE_KEY);
                if(loreElement != null) {
                    for (JsonElement jsonElement : loreElement.getAsJsonArray())
                        tagList.add(StringTag.valueOf(jsonElement.toString()));
                    displayCompound.put(ItemStackSerializer.LORE_KEY, tagList);
                }

                tag.put(key, displayCompound);
            } else {
                Tag base = ItemStackSerializer.jsonElementToNBT(entry.getValue());
                tag.put(key, base);
            }
        }
        return tag;
    }

    /**
     * Load an NBTTag from a given string.
     * @param nbtJson The NBT data as JSON
     * @return nbtData
     */
    public static CompoundTag parseNBT(String nbtJson) {
        JsonElement element = JsonSerializer.JSON_PARSER.parse(nbtJson);
        if(!element.isJsonObject())
            return new CompoundTag();

        return parseNBT(element.getAsJsonObject());
    }

    /**
     * Is the given item a custom item?
     * @param item The item to check
     * @return isCustomItem
     */
    public static boolean isCustomItem(ItemStack item) {
        return getType(item) != null;
    }

    /**
     * Does this ItemStack have a given flag?
     * @param item The ItemStack to check
     * @param tag The tag to test
     * @return hasFlag
     */
    public static boolean hasFlag(ItemStack item, ItemTag tag) {
        CompoundTag nbtTag = getNBTTag(item);
        return nbtTag != null && nbtTag.getBoolean(tag.getNBTTag());
    }

    @AllArgsConstructor
    @Getter
    public enum ItemTag {
        SOULBOUND(ChatColor.GOLD + ChatColor.ITALIC.toString() + "Soulbound"),
        UNTRADEABLE(ChatColor.DARK_GREEN + "[Untradeable Item]");

        private final String displayName;

        public String getNBTTag() {
            return this.name().toLowerCase();
        }
    }
}
