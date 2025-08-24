package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.util.AsyncRateLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {

    private final AsyncRateLimiter rateLimiter = AsyncRateLimiter.getInstance();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (rateLimiter != null) {
            rateLimiter.clearPlayerDataAsync(event.getPlayer());
        }
    }
}
