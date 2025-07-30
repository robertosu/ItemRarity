package cl.nightcore.itemrarity.util;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    // Mapa para almacenar los cooldowns por jugador y tipo de acción
    private final Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();

    // Sistema de detección de spam malicioso
    private final Map<UUID, List<Long>> playerActionHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerWarnings = new ConcurrentHashMap<>();

    // Configuración de cooldowns por tipo de objeto (en milisegundos)
    private static final Map<String, Long> COOLDOWN_CONFIG = Map.of(
            "IDENTIFY_SCROLL", 100L,        // 0.1 segundos
            "MAGIC_OBJECT", 500L,           // 0.5 segundos
            "BLESSING_OBJECT", 750L,        // 0.75 segundos
            "REDEMPTION_OBJECT", 1000L,     // 1 segundo
            "GEM", 200L,                    // 0.2 segundos
            "GEM_REMOVER", 1500L,           // 1.5 segundos
            "BLESSING_BALL", 800L,          // 0.8 segundos
            "ITEM_UPGRADER", 1200L,         // 1.2 segundos
            "SOCKET_STONE", 600L,           // 0.6 segundos
            "XP_MULTIPLIER", 300L           // 0.3 segundos
    );

    // Configuración de detección de spam
    private static final int MAX_ACTIONS_PER_SECOND = 15;    // Máximo 15 acciones por segundo
    private static final int MAX_ACTIONS_PER_5_SECONDS = 40; // Máximo 40 acciones en 5 segundos
    private static final long SPAM_DETECTION_WINDOW = 5000L; // Ventana de 5 segundos
    private static final int MAX_WARNINGS = 3;               // Máximo 3 advertencias antes del kick
    private static final long WARNING_RESET_TIME = 300000L;  // Reset advertencias después de 5 minutos

    // Instancia singleton
    private static RateLimiter instance;

    private RateLimiter() {}

    public static RateLimiter getInstance() {
        if (instance == null) {
            instance = new RateLimiter();
        }
        return instance;
    }

    /**
     * Verifica si el jugador puede realizar la acción y detecta spam malicioso
     * @param player El jugador
     * @param actionType El tipo de acción (debe coincidir con ObjectType)
     * @return SpamCheckResult con el resultado de la verificación
     */
    public SpamCheckResult checkSpamAndCooldown(Player player, String actionType) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Registrar la acción en el historial
        recordPlayerAction(playerId, currentTime);

        // Verificar patrones sospechosos
        SpamLevel spamLevel = detectSpamPatterns(playerId, currentTime);

        if (spamLevel != SpamLevel.NONE) {
            return handleSpamDetection(player, spamLevel);
        }

        // Verificar cooldown normal
        String key = generateKey(playerId, actionType);
        Long lastActionTime = playerCooldowns.get(key);

        if (lastActionTime == null) {
            playerCooldowns.put(key, currentTime);
            return new SpamCheckResult(true, SpamLevel.NONE, 0L);
        }

        long cooldownDuration = COOLDOWN_CONFIG.getOrDefault(actionType, 1000L);
        long timeDifference = currentTime - lastActionTime;

        if (timeDifference >= cooldownDuration) {
            playerCooldowns.put(key, currentTime);
            return new SpamCheckResult(true, SpamLevel.NONE, 0L);
        }

        long remainingCooldown = cooldownDuration - timeDifference;
        return new SpamCheckResult(false, SpamLevel.NONE, remainingCooldown);
    }

    /**
     * Registra una acción del jugador en el historial
     */
    private void recordPlayerAction(UUID playerId, long timestamp) {
        playerActionHistory.computeIfAbsent(playerId, k -> new ArrayList<>()).add(timestamp);
    }

    /**
     * Detecta patrones de spam sospechosos
     */
    private SpamLevel detectSpamPatterns(UUID playerId, long currentTime) {
        List<Long> actions = playerActionHistory.get(playerId);
        if (actions == null || actions.isEmpty()) {
            return SpamLevel.NONE;
        }

        // Limpiar acciones antiguas (fuera de la ventana de detección)
        actions.removeIf(time -> (currentTime - time) > SPAM_DETECTION_WINDOW);

        // Contar acciones en diferentes ventanas de tiempo
        long actionsInLastSecond = actions.stream()
                .mapToLong(time -> currentTime - time)
                .filter(diff -> diff <= 1000L)
                .count();

        long actionsInLast5Seconds = actions.size();

        // Detectar patrones extremadamente sospechosos (bots/macros)
        if (actionsInLastSecond >= MAX_ACTIONS_PER_SECOND) {
            return SpamLevel.CRITICAL;
        }

        // Detectar spam moderado
        if (actionsInLast5Seconds >= MAX_ACTIONS_PER_5_SECONDS) {
            return SpamLevel.HIGH;
        }

        // Detectar patrones regulares sospechosos
        if (actionsInLast5Seconds >= 25) {
            return SpamLevel.MODERATE;
        }

        return SpamLevel.NONE;
    }

    /**
     * Maneja la detección de spam
     */
    private SpamCheckResult handleSpamDetection(Player player, SpamLevel spamLevel) {
        UUID playerId = player.getUniqueId();
        int currentWarnings = playerWarnings.getOrDefault(playerId, 0);

        switch (spamLevel) {
            case CRITICAL:
                // Spam crítico - kick inmediato
                return new SpamCheckResult(false, SpamLevel.CRITICAL, 0L);

            case HIGH:
                currentWarnings++;
                playerWarnings.put(playerId, currentWarnings);

                if (currentWarnings >= MAX_WARNINGS) {
                    return new SpamCheckResult(false, SpamLevel.HIGH, 0L);
                }
                break;

            case MODERATE:
                currentWarnings++;
                playerWarnings.put(playerId, Math.min(currentWarnings, MAX_WARNINGS - 1));
                break;
        }

        return new SpamCheckResult(false, spamLevel, 0L);
    }

    /**
     * Limpia los cooldowns expirados para optimizar memoria
     * Debe ser llamado periódicamente por un scheduler
     */
    public void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        long maxCooldown = COOLDOWN_CONFIG.values().stream().mapToLong(Long::longValue).max().orElse(1000L);

        // Limpiar cooldowns expirados
        playerCooldowns.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > maxCooldown * 2
        );

        // Limpiar historial de acciones antiguas
        playerActionHistory.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(time -> (currentTime - time) > SPAM_DETECTION_WINDOW * 2);
            return entry.getValue().isEmpty();
        });

        // Reset advertencias después del tiempo especificado
        playerWarnings.entrySet().removeIf(entry -> {
            // Simplificado: resetear todas las advertencias en cleanup
            // En implementación real, deberías trackear timestamp de última advertencia
            return false; // Mantener por ahora, implementar lógica más compleja si es necesario
        });
    }

    /**
     * Limpia todos los datos de un jugador cuando se desconecta
     * @param player El jugador que se desconectó
     */
    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        String playerPrefix = playerId.toString() + ":";

        // Limpiar cooldowns
        playerCooldowns.entrySet().removeIf(entry ->
                entry.getKey().startsWith(playerPrefix)
        );

        // Limpiar historial de acciones
        playerActionHistory.remove(playerId);

        // Limpiar advertencias
        playerWarnings.remove(playerId);
    }

    /**
     * Obtiene el número de advertencias de un jugador
     */
    public int getPlayerWarnings(Player player) {
        return playerWarnings.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Resetea las advertencias de un jugador (para comandos de admin)
     */
    public void resetPlayerWarnings(Player player) {
        playerWarnings.remove(player.getUniqueId());
    }

    /**
     * Genera una clave única para el mapa de cooldowns
     * @param playerId UUID del jugador
     * @param actionType Tipo de acción
     * @return Clave única
     */
    private String generateKey(UUID playerId, String actionType) {
        return playerId.toString() + ":" + actionType;
    }

    /**
     * Obtiene el cooldown configurado para un tipo de acción
     * @param actionType El tipo de acción
     * @return Duración del cooldown en milisegundos
     */
    public long getCooldownDuration(String actionType) {
        return COOLDOWN_CONFIG.getOrDefault(actionType, 1000L);
    }

    // Enums y clases de resultado
    public enum SpamLevel {
        NONE,
        MODERATE,  // Advertencia
        HIGH,      // Advertencia fuerte
        CRITICAL   // Kick inmediato
    }

    public static class SpamCheckResult {
        private final boolean allowed;
        private final SpamLevel spamLevel;
        private final long remainingCooldown;

        public SpamCheckResult(boolean allowed, SpamLevel spamLevel, long remainingCooldown) {
            this.allowed = allowed;
            this.spamLevel = spamLevel;
            this.remainingCooldown = remainingCooldown;
        }

        public boolean isAllowed() { return allowed; }
        public SpamLevel getSpamLevel() { return spamLevel; }
        public long getRemainingCooldown() { return remainingCooldown; }
        public boolean shouldKick() {
            return spamLevel == SpamLevel.CRITICAL || spamLevel == SpamLevel.HIGH;
        }
    }
}