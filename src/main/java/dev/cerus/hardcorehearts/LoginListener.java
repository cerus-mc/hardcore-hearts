package dev.cerus.hardcorehearts;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutLogin;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.ITextFilter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener {

    private static Field FILTER_FIELD;

    static {
        for (final Field declaredField : EntityPlayer.class.getDeclaredFields()) {
            if (declaredField.getType() == ITextFilter.class) {
                FILTER_FIELD = declaredField;
                break;
            }
        }
        if (FILTER_FIELD == null) {
            throw new IllegalStateException("Could not find filter field");
        }
        FILTER_FIELD.setAccessible(true);
    }

    private final HardcoreHeartsPlugin plugin;

    public LoginListener(final HardcoreHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        try {
            final Method getHandleMethod = event.getPlayer().getClass().getDeclaredMethod("getHandle");
            getHandleMethod.setAccessible(true);
            final EntityPlayer playerHandle = (EntityPlayer) getHandleMethod.invoke(event.getPlayer());

            final Object filter = FILTER_FIELD.get(playerHandle);
            ReflectionUtil.setPrivateFinalField(
                    FILTER_FIELD,
                    playerHandle,
                    new SpyingTextFilter((ITextFilter) filter, () -> this.handleCallback(playerHandle))
            );
        } catch (final NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to inject into player - Are you running an unsupported Java / Minecraft version?", e);
        }
    }

    private void handleCallback(final EntityPlayer handle) {
        final ChannelHandler handler = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
                if (msg instanceof final PacketPlayOutLogin login && LoginListener.this.plugin.isHeartsEnabled()) {
                    // Clone packet and change hardcore boolean
                    // Changing the field using reflection does not work for some reason,
                    // if you do that the client does not display any blocks
                    final PacketPlayOutLogin fakeLogin = new PacketPlayOutLogin(login.b(),
                            true,
                            login.f(),
                            login.g(),
                            login.h(),
                            login.i(),
                            login.j(),
                            login.k(),
                            login.l(),
                            login.m(),
                            login.n());
                    super.write(ctx, fakeLogin, promise);
                } else {
                    super.write(ctx, msg, promise);
                }
            }
        };

        final NetworkManager netMan;
        try {
            final Field netManField = handle.c.getClass().getSuperclass().getDeclaredField("e");
            netManField.setAccessible(true);
            netMan = (NetworkManager) netManField.get(handle.c);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        netMan.n.pipeline().addBefore("packet_handler", "hardcore_injector", handler);
    }

}
