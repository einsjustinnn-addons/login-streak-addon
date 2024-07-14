package de.einsjustinnn.core;

import de.einsjustinnn.core.config.LoginStreakConfiguration;
import de.einsjustinnn.core.listeners.DisconnectListener;
import de.einsjustinnn.core.tags.LoginSteakTag;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class LoginStreakAddon extends LabyAddon<LoginStreakConfiguration> {

  private static LoginStreakAddon addon;

  @Override
  protected void enable() {

    addon = this;

    this.registerSettingCategory();

    registerListener(new DisconnectListener());

    labyAPI().tagRegistry().register("login_streak", PositionType.BELOW_NAME, new LoginSteakTag());
  }

  @Override
  protected Class<LoginStreakConfiguration> configurationClass() {
    return LoginStreakConfiguration.class;
  }

  public static LoginStreakAddon getAddon() {
    return addon;
  }
}
