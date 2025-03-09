package net.sabafly.emeraldbank.external;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OpenInvAccess {

    private OpenInvAccess() {
    }

    private static boolean openInvEnabled = false;

    public static void load() {
        openInvEnabled = Bukkit.getPluginManager().isPluginEnabled("OpenInv");
    }

    @Nullable
    public static Player getOpenInvPlayer(OfflinePlayer player) {
        if (player.isOnline()) {
            return player.getPlayer();
        }
        if (openInvEnabled) {
            return ((com.lishid.openinv.IOpenInv) Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("OpenInv"))).loadPlayer(player);
        }
        return null;
    }

}
