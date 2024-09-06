package io.gameshield.bukkit;

import io.gameshield.bukkit.command.InformationCommand;
import io.gameshield.bukkit.injector.ChannelInjector;
import io.gameshield.bukkit.injector.standard.ProtocolLibChannelInjector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

/**
 * @author milansky
 */
@Plugin(name = "GameShield", version = "1.0.0")
@Dependency("ProtocolLib")
@Commands({
        @Command(name = "gameshield", desc = "GameShield command", usage = "/gameshield")
})
public final class GameShieldPlugin extends JavaPlugin {
    private static final ChannelInjector CHANNEL_INJECTOR = ProtocolLibChannelInjector.create();

    @Override
    public void onEnable() {
        getCommand("gameshield").setExecutor(InformationCommand.create());

        CHANNEL_INJECTOR.inject();
    }
}
