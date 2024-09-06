package io.gameshield.bukkit.injector.standard;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.netty.manager.NetworkManagerInjector;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.gameshield.bukkit.GameShieldPlugin;
import io.gameshield.bukkit.handler.HaProxyMessageHandler;
import io.gameshield.bukkit.injector.ChannelInjector;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author milansky
 */
@NoArgsConstructor(staticName = "create")
public final class ProtocolLibChannelInjector implements ChannelInjector {
    private static final Field NETWORK_MANAGER_INJECTOR, PIPELINE_INJECTOR_HANDLER, CHANNEL_INBOUND_HANDLER;

    static {
        try {
            NETWORK_MANAGER_INJECTOR = PacketFilterManager.class.getDeclaredField("networkManagerInjector");
            NETWORK_MANAGER_INJECTOR.setAccessible(true);

            PIPELINE_INJECTOR_HANDLER = NetworkManagerInjector.class.getDeclaredField("pipelineInjectorHandler");
            PIPELINE_INJECTOR_HANDLER.setAccessible(true);

            CHANNEL_INBOUND_HANDLER = PIPELINE_INJECTOR_HANDLER.getType().getDeclaredField("handler");
            CHANNEL_INBOUND_HANDLER.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull ChannelHandler getNetworkManager(final @NotNull ChannelPipeline pipeline) {
        val networkManagerClass = (Class<? extends ChannelHandler>) MinecraftReflection.getNetworkManagerClass();

        for (val entry : pipeline)
            if (networkManagerClass.isAssignableFrom(entry.getValue().getClass()))
                return entry.getValue();

        throw new IllegalArgumentException("NetworkManager not found in channel pipeline " + pipeline.names());
    }

    @Override
    @SneakyThrows
    public void inject() {
        val networkManagerInjector = NETWORK_MANAGER_INJECTOR.get(ProtocolLibrary.getProtocolManager());
        val pipelineInjectorHandler = PIPELINE_INJECTOR_HANDLER.get(networkManagerInjector);
        val channelInboundHandler = (ChannelInboundHandler) CHANNEL_INBOUND_HANDLER.get(pipelineInjectorHandler);

        val haProxyHandler = (ChannelInboundHandler) Proxy.newProxyInstance(GameShieldPlugin.class.getClassLoader(),
                new Class[]{ChannelInboundHandler.class}, ChannelInboundHandlerInvocationHandler.create(channelInboundHandler));

        CHANNEL_INBOUND_HANDLER.set(pipelineInjectorHandler, haProxyHandler);
    }

    @RequiredArgsConstructor(staticName = "create")
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    private static final class ChannelInboundHandlerInvocationHandler implements InvocationHandler {
        private static final Method CHANNEL_ACTIVE_METHOD;

        static {
            try {
                CHANNEL_ACTIVE_METHOD = ChannelInboundHandler.class.getDeclaredMethod("channelActive", ChannelHandlerContext.class);
            } catch (final NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        ChannelInboundHandler channelInboundHandler;

        @Override
        public Object invoke(
                final @NotNull Object proxy,
                final @NotNull Method method,
                final @NotNull Object[] args
        ) throws Throwable {
            if (!method.equals(CHANNEL_ACTIVE_METHOD)) return method.invoke(channelInboundHandler, args);

            val ctx = (ChannelHandlerContext) args[0];

            ctx.pipeline().remove((ChannelHandler) proxy)
                    .addFirst("protocol_lib_inbound_inject", channelInboundHandler);

            val returnValue = method.invoke(channelInboundHandler, args);
            val channelPipeline = ctx.channel().pipeline();

            channelPipeline.addFirst("haproxy-decoder", new HAProxyMessageDecoder());
            channelPipeline.addAfter("haproxy-decoder", "haproxy-handler",
                    HaProxyMessageHandler.create(getNetworkManager(channelPipeline)));

            return returnValue;
        }
    }
}
