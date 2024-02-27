package de.einsjustinnn.core.tags;

import de.einsjustinnn.core.LoginStreakAddon;
import de.einsjustinnn.core.utils.LoginStreakApi;
import de.einsjustinnn.core.utils.LoginStreakCache;
import java.util.UUID;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class LoginSteakTag extends NameTag {

  private final LoginStreakApi loginStreakApi;

  private int streak;

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
  public float getWidth() {
    return super.getWidth() + getHeight();
  }

  @Override
  protected void renderText(Stack stack, RenderableComponent component, boolean discrete, int textColor, int backgroundColor, float x, float y) {

    if (!LoginStreakAddon.getAddon().configuration().enabled().get()) return;
    if (getRenderableComponent() == null) return;

    Icon icon = Icon.texture(ResourceLocation.create("loginstreak", "textures/icon_streak.png"));

    RenderPipeline renderPipeline = Laby.references().renderPipeline();

    renderPipeline.rectangleRenderer().renderRectangle(stack, x, y, getWidth(), getHeight(), backgroundColor);

    renderPipeline.renderSeeThrough(entity, () -> icon.render(stack, x + 1, y + 1, getHeight() - 3));

    float textX = x + getHeight();

    super.renderText(stack, component, discrete, textColor, 0, textX, y + 1);
  }

  @Override
  protected @Nullable RenderableComponent getRenderableComponent() {

    if (!LoginStreakAddon.getAddon().configuration().enabled().get()) return null;
    if (!(entity instanceof Player player)) return null;
    if (player.getNetworkPlayerInfo() == null) return null;

    UUID uuid = player.getUniqueId();

    if (LoginStreakCache.loginStreaks.containsKey(uuid)) {
      streak = LoginStreakCache.loginStreaks.get(uuid);
    } else {
      if (!LoginStreakCache.resolving.contains(uuid)) {

        LoginStreakCache.resolving.add(uuid);

        LoginStreakAddon.getAddon().logger().debug("resolve. " + player.getName() + ":" + player.getUniqueId().toString());

        loginStreakApi.getLoginStreak(uuid).thenAccept(newStreak -> {
          LoginStreakCache.loginStreaks.put(uuid, newStreak);
          LoginStreakCache.resolving.remove(uuid);
          LoginStreakAddon.getAddon().logger().debug("remove resolved from Cache. " + player.getName() + ":" + player.getUniqueId().toString());
          streak = newStreak;
        });
      }
      return null;
    }

    if (streak == 0 && LoginStreakAddon.getAddon().configuration().hideZero().get()) {
      return null;
    }

    String streakString = formatStreak(streak);

    return RenderableComponent.of(Component.text(streakString));
  }

  private String formatStreak(int streak) {
    if (streak > 365) {
      return "§6§o" + streak;
    } else {
      return "§e§o" + streak;
    }
  }
}
