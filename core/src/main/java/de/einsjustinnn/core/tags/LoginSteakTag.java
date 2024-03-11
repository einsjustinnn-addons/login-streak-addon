package de.einsjustinnn.core.tags;

import de.einsjustinnn.core.LoginStreakAddon;
import de.einsjustinnn.core.utils.LoginStreakApi;
import de.einsjustinnn.core.utils.LoginStreakCache;
import de.einsjustinnn.core.utils.StreakResolver;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
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

  public LoginSteakTag() {
    loginStreakApi = new LoginStreakApi();
  }

  @Override
  public boolean isVisible() {
    if (LoginStreakAddon.getAddon().configuration().enabled().get() && !this.entity.isCrouching()) {
      if (streak == 0 && LoginStreakAddon.getAddon().configuration().hideHiddenStreak().get()) {
        return false;
      }
      return LoginStreakCache.loginStreaks.containsKey(entity.getUniqueId());
    }
    return false;
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
        return null;
      }

      if (!LoginStreakCache.resolving.contains(uuid)) {
        requestStreak(player);
      }

      return null;
    }

    if (streak == 0 && LoginStreakAddon.getAddon().configuration().hideHiddenStreak().get()) {
      return null;
    }

    Component component = streak == 0 ? Component.translatable("loginstreak.hidden", NamedTextColor.RED)
        : Component.text(streak, streak > 365 ? NamedTextColor.GOLD : NamedTextColor.YELLOW);

    return RenderableComponent.of(component);
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
}
