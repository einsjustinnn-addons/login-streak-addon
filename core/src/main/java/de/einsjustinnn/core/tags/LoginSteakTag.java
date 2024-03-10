package de.einsjustinnn.core.tags;

import de.einsjustinnn.core.LoginStreakAddon;
import de.einsjustinnn.core.utils.LoginStreakApi;
import de.einsjustinnn.core.utils.LoginStreakCache;
import de.einsjustinnn.core.utils.StreakResolver;
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

import java.util.UUID;

public class LoginSteakTag extends NameTag {

  private final LoginStreakApi loginStreakApi;
  private int streak;
  private long rateLimited;
  private int tempRate;

  public LoginSteakTag() {
    loginStreakApi = new LoginStreakApi();
  }

  @Override
  public boolean isVisible() {
    return !this.entity.isCrouching() && LoginStreakAddon.getAddon().configuration().enabled().get();
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
      int streak1 = LoginStreakCache.loginStreaks.getOrDefault(uuid, -1);

      if (streak1 == -1) {
        requestStreak(player);
      }

      streak = streak1;
    } else {
      if (System.currentTimeMillis() < rateLimited) {
        int rateLimitSeconds = (int) ((rateLimited - System.currentTimeMillis()) / 1000);
        if (tempRate == 0) {
          tempRate = rateLimitSeconds;
        }
        if (tempRate >= rateLimitSeconds) {
          LoginStreakAddon.getAddon().logger().info("currently in the rate limit for " + rateLimitSeconds + " seconds.");
          tempRate--;
        }
        return null;
      }

      if (!LoginStreakCache.resolving.contains(uuid)) {
        requestStreak(player);
      }

      return null;
    }

    if (streak == 0 && LoginStreakAddon.getAddon().configuration().hideZero().get()) {
      return null;
    }

    String streakString = (streak == -2) ? "§7hide" : formatStreak(streak);
    return RenderableComponent.of(Component.text(streakString));
  }

  private void requestStreak(Player player) {
    UUID uuid = player.getUniqueId();

    LoginStreakCache.resolving.add(uuid);

    LoginStreakAddon.getAddon().logger().debug("Resolve data from  " + player.getName() + ":" + player.getUniqueId().toString());

    loginStreakApi.resolveHandler(uuid, new StreakResolver() {
      @Override
      public void onSuccess(int count) {
        LoginStreakCache.loginStreaks.put(uuid, count);
        LoginStreakCache.resolving.remove(uuid);
        LoginStreakAddon.getAddon().logger().debug("Data resolved from " + player.getName() + ":" + player.getUniqueId().toString());
        streak = count;
      }

      @Override
      public void onFailed(int responseCode) {
        if (responseCode == 429) {
          rateLimited = System.currentTimeMillis() + 30000;
          LoginStreakAddon.getAddon().logger().debug("Rate Limit occurred.");
        }
      }
    });
  }

  private String formatStreak(int streak) {
    return (streak > 365) ? "§6§o" + streak : "§e§o" + streak;
  }
}
