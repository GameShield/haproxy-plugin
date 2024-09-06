package io.gameshield.bukkit.command;

import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author milansky
 */
@NoArgsConstructor(staticName = "create")
public final class InformationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        sender.sendMessage("§d§lGame§5§lShield §dplugin made by milansky");
        sender.sendMessage("§dhttps://gameshield.io");

        return true;
    }
}
