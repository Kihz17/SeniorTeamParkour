package com.kihz.utils.jsontools.serializers.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kihz.Core;
import com.kihz.item.GUIItem;
import com.kihz.item.ItemManager;
import com.kihz.item.ItemWrapper;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.serializers.Serializer;
import net.minecraft.nbt.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

public class ItemStackSerializer extends Serializer<ItemStack> {
    public static final String MATERIAL_KEY = "type";
    public static final String AMOUNT_KEY = "amt";
    public static final String DISPLAY_KEY = "display";
    public static final String NAME_KEY = "Name";
    public static final String LORE_KEY = "Lore";

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Class<ItemStack> loadClass, Field field) {
        if(!jsonElement.isJsonObject())
            return null;

        JsonObject itemJson = jsonElement.getAsJsonObject();
        Material itemType = Material.valueOf(itemJson.get(MATERIAL_KEY).getAsString());
        int amount = itemJson.has(AMOUNT_KEY) ? itemJson.get(AMOUNT_KEY).getAsInt() : 1;

        ItemStack itemStack = new ItemStack(itemType, amount);
        JsonElement nbtElement = itemJson.get(ItemManager.ITEM_NBT_TAG);
        if(nbtElement != null && nbtElement.isJsonObject()) {
            JsonObject nbtJson = nbtElement.getAsJsonObject();
            itemStack = ItemWrapper.setNBTTag(itemStack, ItemWrapper.parseNBT(nbtJson
            ));
        }

        return itemStack;
    }

    @Override
    public JsonElement serialize(ItemStack value) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty(MATERIAL_KEY, value.getType().name());
        itemJson.addProperty(AMOUNT_KEY, value.getAmount());

        CompoundTag nbtTag = ItemWrapper.getNBTTag(value);
        if(nbtTag != null) { // Save NBTTag data if we have it
            // We need to do extra parsing here to make sure we don't end up with a bunch of backslashes in our JSON string
            JsonObject tagObj = new JsonObject();
            parseNBTTag(nbtTag, tagObj);
            itemJson.add(ItemManager.ITEM_NBT_TAG, tagObj);
        }
        return itemJson;
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        ItemStack i = (ItemStack) value;
        item.setIcon(Material.STICK)
                .addLore("Item: " + ChatColor.GOLD + Utils.getItemName(i), "");
        setNull(item, value, setter);
    }

    /**
     * Parse an NBT tag
     * @param nbtTag The tag to parse
     * @param jsonObject The object to parse into
     */
    private void parseNBTTag(CompoundTag nbtTag, JsonObject jsonObject) {
        for(String key : nbtTag.getAllKeys()) {
            Tag base = nbtTag.get(key);
            if(base instanceof CompoundTag) {
                JsonObject compoundObject = new JsonObject();
                parseNBTTag((CompoundTag) base, compoundObject); // Recursively add compound objects
                jsonObject.add(key, compoundObject);
            } else if(base instanceof StringTag) {
                if(key.equals(NAME_KEY)) {
                    jsonObject.add(key, JsonSerializer.JSON_PARSER.parse(base.getAsString()));
                } else {
                    jsonObject.add(key, new JsonPrimitive(base.getAsString()));
                }
            } else if(base instanceof ByteTag) {
                jsonObject.addProperty(key, ((ByteTag) base).getAsByte());
            } else if(base instanceof ShortTag) {
                jsonObject.addProperty(key, ((ShortTag) base).getAsShort());
            } else if(base instanceof IntTag) {
                jsonObject.addProperty(key, ((IntTag) base).getAsInt());
            } else if(base instanceof LongTag) {
                jsonObject.addProperty(key, ((LongTag) base).getAsLong());
            } else if(base instanceof FloatTag) {
                jsonObject.addProperty(key, ((FloatTag) base).getAsFloat());
            } else if(base instanceof DoubleTag) {
                jsonObject.addProperty(key, ((DoubleTag) base).getAsDouble());
            } else if(base instanceof CollectionTag) {
                JsonArray jsonArr = new JsonArray();
                boolean loreTag = key.equals(LORE_KEY);
                ListTag list = (ListTag) base;
                for(int i = 0; i < list.size(); i++) {
                    Tag value = list.get(i);

                    if(loreTag) {
                        String s = value.getAsString();
                        try {
                            JsonElement ele = JsonSerializer.JSON_PARSER.parse(s);
                            jsonArr.add(ele);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Core.logInfo("Found a malformed JSON string while serializing an item!");
                        }
                    } else if(value instanceof ByteTag) {
                        jsonArr.add(((ByteTag) value).getAsByte());
                    } else if(value instanceof IntTag) {
                        jsonArr.add(((IntTag) value).getAsInt());
                    } else if(value instanceof LongTag) {
                        jsonArr.add(((LongTag) value).getAsLong());
                    } else if(value instanceof FloatTag) {
                        jsonArr.add(((FloatTag) value).getAsFloat());
                    } else if(value instanceof DoubleTag) {
                        jsonArr.add(((DoubleTag) value).getAsDouble());
                    } else if(value instanceof CompoundTag) {
                        JsonObject listObj = new JsonObject();
                        parseNBTTag((CompoundTag) value, listObj);
                        jsonArr.add(listObj);
                    } else {
                        jsonArr.add(value.toString());
                    }
                }
                jsonObject.add(key, jsonArr);
            }
        }
    }

    /**
     * Convert a JSON element to its NBT equivalent
     * @param element The element to convert
     * @return nbtItem
     */
    public static Tag jsonElementToNBT(JsonElement element) {
        if(element.isJsonNull()) {
            return StringTag.valueOf("null");
        } else if(element.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
            if(jsonPrimitive.isBoolean()) {
                return StringTag.valueOf(Boolean.toString(jsonPrimitive.getAsBoolean()));
            } else if(jsonPrimitive.isNumber()) {
                Number number = jsonPrimitive.getAsNumber();
                if(number instanceof Byte) {
                    return ByteTag.valueOf(jsonPrimitive.getAsByte());
                } else if(number instanceof Short) {
                    return ShortTag.valueOf(jsonPrimitive.getAsShort());
                } else if(number instanceof Long) {
                    return LongTag.valueOf(jsonPrimitive.getAsLong());
                } else if(number instanceof Float) {
                    return FloatTag.valueOf(jsonPrimitive.getAsFloat());
                } else if(number instanceof Double) {
                    return DoubleTag.valueOf(jsonPrimitive.getAsDouble());
                } else {
                    return IntTag.valueOf(jsonPrimitive.getAsInt());
                }
            } else if(jsonPrimitive.isString()) {
                return StringTag.valueOf(jsonPrimitive.getAsString());
            }
        } else if(element.isJsonArray()) {
            JsonArray jsonArr = element.getAsJsonArray();
            int size = jsonArr.size();
            if(size <= 0) {
                return new ListTag();
            } else {
                ListTag tagList = new ListTag();
                for (JsonElement jsonElement : jsonArr)
                    tagList.add(jsonElementToNBT(jsonElement)); // Recursive call to determing type
                return tagList;
            }
        } else if(element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            CompoundTag tagCompound = new CompoundTag();
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                tagCompound.put(entry.getKey(), jsonElementToNBT(entry.getValue())); // Recursive call to fill compound
            return tagCompound;
        }

        return StringTag.valueOf(element.toString());
    }

}