package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.util.RateLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {

    private final RateLimiter rateLimiter = RateLimiter.getInstance();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Limpiar todos los datos del jugador cuando se desconecta
        rateLimiter.clearPlayerData(event.getPlayer());
    }
}