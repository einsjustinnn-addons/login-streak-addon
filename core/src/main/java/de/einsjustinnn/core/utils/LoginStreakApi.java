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

  public CompletableFuture<Integer> getLoginStreak(UUID uuid) {

    return CompletableFuture.supplyAsync(() -> {

      int streak = 0;

      try {

        URL url = new URL("https://laby.net/api/v3/user/" + uuid + "/game-stats");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

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

      } catch (IOException e) {
        LoginStreakAddon.getAddon().logger().debug("No data could be retrieved from " + uuid + ". " + e.getMessage());
      }

      return streak;
    });
  }
}
