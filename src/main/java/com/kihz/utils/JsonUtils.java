package com.kihz.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kihz.Core;
import lombok.Cleanup;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {
    public static JsonParser JSON_PARSER = new JsonParser();

    /**
     * Is this json element null?
     * @param jsonElement The element to test
     * @return isJsonNull
     */
    public static boolean isJsonNull(JsonElement jsonElement) {
        return jsonElement == null || jsonElement.isJsonNull();
    }

    /**
     * Read JSON data from a file.
     * @param file The file to read from.
     * @return jsonElement
     */
    public static JsonElement readJsonFile(@NonNull File file) {
        try {
            @Cleanup FileReader fileReader = new FileReader(file);
            @Cleanup BufferedReader bufferedReader = new BufferedReader(fileReader);
            return JSON_PARSER.parse(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
            Core.logInfo("Failed to read JSON file %s.", file.getName());
            return null;
        }
    }
}
