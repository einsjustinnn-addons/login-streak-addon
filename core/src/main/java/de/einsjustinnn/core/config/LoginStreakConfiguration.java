package de.einsjustinnn.core.config;

import de.einsjustinnn.core.LoginStreakAddon;
import de.einsjustinnn.core.utils.LoginStreakCache;
import net.labymod.api.Laby;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.notification.Notification;
import net.labymod.api.util.MethodOrder;

@ConfigName("settings")
@SpriteTexture(value = "settings")
public class LoginStreakConfiguration extends AddonConfig {

  @SwitchSetting
  @SpriteSlot()
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SliderSetting(min = 3, max = 10)
  @SpriteSlot(x = 1)
  private final ConfigProperty<Integer> scale = new ConfigProperty<>(10);

  @SwitchSetting
  @SpriteSlot(x = 2)
  private final ConfigProperty<Boolean> hideZero = new ConfigProperty<>(true);


  @SwitchSetting
  private final ConfigProperty<Boolean> hideHidedStreak = new ConfigProperty<>(true);

  @MethodOrder(after = "enabled")
  @ButtonSetting
  @SuppressWarnings("unused")
  public void clearCache() {
    LoginStreakCache.loginStreaks.clear();
    Notification notification = Notification.builder()
        .title(Component.translatable("loginstreak.settings.name")).text(Component.translatable("loginstreak.settings.clearCache.notification"))
        .icon(Icon.texture(ResourceLocation.create("loginstreak", "textures/icon.png")))
        .build();
    Laby.labyAPI().notificationController().push(notification);
    LoginStreakAddon.getAddon().logger().debug("Cache cleared.");
  }

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Integer> scale() {
    return scale;
  }

  public ConfigProperty<Boolean> hideZero() {
    return hideZero;
  }

  public ConfigProperty<Boolean> hideHidedStreak() {
    return hideHidedStreak;
  }
}
