package de.einsjustinnn.core.tags;

import java.util.UUID;
import de.einsjustinnn.core.LoginStreakAddon;
import de.einsjustinnn.core.utils.LoginStreakApi;
import de.einsjustinnn.core.utils.LoginStreakCache;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class LoginSteakTag extends NameTag {

  private final LoginStreakApi loginStreakApi;

  public LoginSteakTag() {
    loginStreakApi = new LoginStreakApi();
  }

  @Override
  public boolean isVisible() {
    return LoginStreakAddon.getAddon().configuration().enabled().get();
  }

  @Override
  public float getScale() {
    return (float) LoginStreakAddon.getAddon().configuration().scale().get() / 10;
  }

  @Override
  public float getHeight() {
    return super.getHeight() + 1;
  }

  @Override
  public float getWidth() {
    return super.getWidth() + getHeight();
  }

  @Override
  protected void renderText(Stack stack, RenderableComponent component, boolean discrete, int textColor, int backgroundColor, float x, float y) {

    if (!LoginStreakAddon.getAddon().configuration().enabled().get()) {
      return;
    }

    Icon icon = Icon.texture(ResourceLocation.create("loginstreak", "textures/icon_streak.png"));

    RenderPipeline renderPipeline = Laby.references().renderPipeline();

    renderPipeline.rectangleRenderer().renderRectangle(stack, x, y, getWidth(), getHeight(), backgroundColor);

    renderPipeline.renderSeeThrough(entity, () -> icon.render(stack, x + 1, y + 1, getHeight() - 2));

    float h = getHeight();

    float textX = x;

    textX += h + 1;

    super.renderText(stack, component, discrete, textColor, 0, textX, y + 1);
  }

  @Override
  protected @Nullable RenderableComponent getRenderableComponent() {

    if (!LoginStreakAddon.getAddon().configuration().enabled().get()) {
      return null;
    }

    if (!(entity instanceof Player player)) {
      return null;
    }

    NetworkPlayerInfo networkPlayerInfo = player.getNetworkPlayerInfo();
    if (networkPlayerInfo == null) {
      return null;
    }

    UUID uuid = player.getUniqueId();

    int streak = LoginStreakCache.loginStreaks.getOrDefault(uuid, -1);

    if (streak == 0 && LoginStreakAddon.getAddon().configuration().hideZero().get()) {
      return null;
    }

    if (!LoginStreakCache.loginStreaks.containsKey(uuid)) {
      loginStreakApi.getLoginStreak(uuid).thenAccept(newStreak -> {
        LoginStreakCache.loginStreaks.put(uuid, newStreak);
        LoginStreakCache.resolving.remove(uuid);
      });
    }

    String streakString = formatStreak(streak);

    return RenderableComponent.of(Component.text(streakString), HorizontalAlignment.LEFT);
  }

  private String formatStreak(int streak) {
    if (streak > 365) {
      return "§6§o" + streak;
    } else {
      return "§e§o" + streak;
    }
  }
}
