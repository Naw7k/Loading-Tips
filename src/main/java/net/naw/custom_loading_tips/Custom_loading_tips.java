package net.naw.custom_loading_tips;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Custom_loading_tips implements ModInitializer {
    public static final String MOD_ID = "custom_loading_tips";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final List<String> LOADING_TIPS = new ArrayList<>();

    @Override
    public void onInitialize() {
        // FIRST: Add a dummy tip so we can see if the Mixin works
        LOADING_TIPS.add("Failsafe: Mixin is working, but JSON not found!");
        loadTips();
    }

    private void loadTips() {
        try {
            // This path must match your folder exactly
            var resource = getClass().getClassLoader().getResourceAsStream("assets/custom_loading_tips/text/loading_tips.json");

            if (resource != null) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonArray array = json.getAsJsonArray("tips");

                LOADING_TIPS.clear(); // Remove the failsafe if we found the file
                for (int i = 0; i < array.size(); i++) {
                    LOADING_TIPS.add(array.get(i).getAsString());
                }
                LOGGER.info("!!! [SUCCESS] Loaded {} tips!", LOADING_TIPS.size());
            } else {
                LOGGER.error("!!! [ERROR] Could not find loading_tips.json!");
            }
        } catch (Exception e) {
            LOGGER.error("!!! [ERROR] Failed to read JSON content!", e);
        }
    }
}