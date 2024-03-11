package de.einsjustinnn.core.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.einsjustinnn.core.LoginStreakAddon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LoginStreakApi {

  public void resolveHandler(UUID uuid, StreakResolver rateLimit) {

    CompletableFuture.supplyAsync(() -> {

      String gameStatsUrl = String.format("https://laby.net/api/v3/user/%s/game-stats", uuid);

      int streak = 0;

      try {

        URL url = new URL(gameStatsUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36");
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(2000);

        if (connection.getResponseCode() == 200) {
          try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder stringBuilder = new StringBuilder();
            String responseLine;

            while ((responseLine = bufferedReader.readLine()) != null) {
              stringBuilder.append(responseLine.trim());
            }

            JsonObject jsonObject = JsonParser.parseString(stringBuilder.toString()).getAsJsonObject();

            if (jsonObject.get("playtime") != null) {
              streak = jsonObject.get("playtime").getAsJsonObject().get("streak").getAsInt();
            }

          } finally {
            connection.disconnect();
          }

          rateLimit.onSuccess(streak);
        } else {
          rateLimit.onFailed(connection.getResponseCode());
        }

      } catch (IOException e) {
        streak = -1;
        LoginStreakAddon.getAddon().logger().debug("No data could be retrieved from " + uuid + ". " + e.getMessage());
      }

      return streak;
    });

  }
}
