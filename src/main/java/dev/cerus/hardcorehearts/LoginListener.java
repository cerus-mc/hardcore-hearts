package dev.cerus.hardcorehearts;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import net.minecraft.network.protocol.game.PacketPlayOutLogin;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.ITextFilter;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener {

    private static Field FILTER_FIELD;

    static {
        try {
            FILTER_FIELD = EntityPlayer.class.getDeclaredField("cW");
            FILTER_FIELD.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private final HardcoreHeartsPlugin plugin;

    public LoginListener(final HardcoreHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        final EntityPlayer playerHandle = ((CraftPlayer) event.getPlayer()).getHandle();
        try {
            final Object filter = FILTER_FIELD.get(playerHandle);
            ReflectionUtil.setPrivateFinalField(
                    FILTER_FIELD,
                    playerHandle,
                    new SpyingTextFilter((ITextFilter) filter, () -> this.handleCallback(playerHandle))
            );
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            this.plugin.getLogger().severe("Failed to inject into player - Are you running an unsupported Java / Minecraft version?");
        }
    }

    private void handleCallback(final EntityPlayer handle) {
        final ChannelHandler handler = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
                if (msg instanceof PacketPlayOutLogin login && LoginListener.this.plugin.isHeartsEnabled()) {
                    // Clone packet and change hardcore boolean
                    // Changing the field using reflection does not work for some reason,
                    // if you do that the client does not display any blocks
                    final PacketPlayOutLogin fakeLogin = new PacketPlayOutLogin(login.b(),
                            true,
                            login.d(),
                            login.e(),
                            login.f(),
                            login.g(),
                            login.h(),
                            login.i(),
                            login.j(),
                            login.k(),
                            login.l(),
                            login.m(),
                            login.n(),
                            login.o(),
                            login.p(),
                            login.q());
                    super.write(ctx, fakeLogin, promise);
                } else {
                    super.write(ctx, msg, promise);
                }
            }
        };
        handle.b.a.m.pipeline().addBefore("packet_handler", "hardcore_injector", handler);
    }

}
