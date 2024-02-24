package de.einsjustinnn.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoginStreakCache {
  public static final List<UUID> resolving = new ArrayList<>();
  public static final Map<UUID, Integer> loginStreaks = new HashMap<>();
}
