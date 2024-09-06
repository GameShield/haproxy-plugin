package io.gameshield.bukkit.handler;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class HaProxyMessageHandler extends SimpleChannelInboundHandler<HAProxyMessage> {
    private static final Field SOCKET_ADDRESS_FIELD;

    static {
        SOCKET_ADDRESS_FIELD = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass(), true)
                .getFieldByType("socketAddress", SocketAddress.class);
        SOCKET_ADDRESS_FIELD.setAccessible(true);
    }

    ChannelHandler handler;

    @Override
    @SneakyThrows
    protected void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull HAProxyMessage message) {
        val socketAddress = new InetSocketAddress(message.sourceAddress(), message.sourcePort());
        SOCKET_ADDRESS_FIELD.set(handler, socketAddress);
    }
}
