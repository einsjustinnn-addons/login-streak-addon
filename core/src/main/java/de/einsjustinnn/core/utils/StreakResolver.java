package de.einsjustinnn.core.utils;

public interface StreakResolver {
  void onSuccess(int count);
  void onFailed(int responseCode);
}
