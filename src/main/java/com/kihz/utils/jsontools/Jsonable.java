package com.kihz.utils.jsontools;

import com.google.gson.JsonElement;
import com.kihz.Core;
import com.kihz.utils.jsontools.serializers.ObjectSerializer;

import java.io.File;
import java.io.FileWriter;

public interface Jsonable {

    /**
     * Load data from JSON. This method is overridable.
     * @param jsonElement
     */
    default void load(JsonElement jsonElement) {
        load0(jsonElement);
    }

    /**
     * The default load method.
     * @param jsonElement The data to load from
     */
    default void load0(JsonElement jsonElement) {
        ObjectSerializer.deserializeFields(jsonElement.getAsJsonObject(), this);
    }

    /**
     * Save this to a JSON object. This method is overridable.
     * @return json
     */
    default JsonElement save() {
        return save0();
    }

    /**
     * Default method to serialize an object into JSON.
     * @return serializedJson
     */
    default JsonElement save0() {
        return JsonSerializer.addClass(this, ObjectSerializer.serializeFields(this));
    }

    /**
     * Save this object to a file.
     * @param file The file to save the Json to.
     */
    default void saveToFile(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(toPrettyJson());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Core.logInfo("Failed to save '%s' as JSON.", file.getName());
        }
    }

    /**
     * Return the json data for this object.
     * Can be supplied into a dup.
     * @return jsonData.
     */
    default String toJsonString() {
        return save().toString();
    }

    /**
     * Return the pretty-print json data of this object.
     * @return prettyJson
     */
    default String toPrettyJson() {
        return JsonSerializer.GSON_PRETTY_PRINT.toJson(save());
    }
}
