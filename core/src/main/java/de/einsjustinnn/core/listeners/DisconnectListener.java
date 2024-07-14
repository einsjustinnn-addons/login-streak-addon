package de.einsjustinnn.core.listeners;

import de.einsjustinnn.core.utils.LoginStreakCache;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;

public class DisconnectListener {

  @Subscribe
  public void onServerDisconnect(ServerDisconnectEvent event) {
    LoginStreakCache.loginStreaks.clear();
    LoginStreakCache.resolving.clear();
  }
}
