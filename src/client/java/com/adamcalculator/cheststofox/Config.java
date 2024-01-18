package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.util.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;

import java.io.File;

public class Config {
    public static Config CONFIG;

    // currently unused
    @SerializedName("version")
    public int version = 1;

    @SerializedName("saving")
    public boolean savingEnable = false;

    @SerializedName("auto_close_guis")
    public boolean autoCloseWhenSaving = false;
    public transient int autoCloseWhenSavingDelay = 200;

    public boolean isSaving() {
        return savingEnable;
    }

    public void setSaving(boolean b) {
        this.savingEnable = b;
        save();
    }

    public void setAutoClose(boolean b) {
        this.autoCloseWhenSaving = b;
        save();
    }

    public void save() {
        File file = getConfigFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this, Config.class);

        Files.writeFile(file, json);
    }

    public static Config loadFromFile() {
        File file = getConfigFile();
        if (file.exists()) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(Files.readFile(file), Config.class);
            } catch (Exception ignored) {}
        }
        return new Config();
    }

    public static File getConfigFile() {
        return new File(MinecraftClient.getInstance().runDirectory, "/config/cheststofox.json");
    }

    public boolean isAutoCloseGuis() {
        return autoCloseWhenSaving;
    }
}
