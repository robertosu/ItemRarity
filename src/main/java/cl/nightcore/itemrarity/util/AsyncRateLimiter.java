package cl.nightcore.itemrarity.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncRateLimiter {

    // Configuración de cooldowns
    private static final Map<String, Long> COOLDOWN_CONFIG = Map.of(
            "IDENTIFY_SCROLL", 500L,
            "MAGIC_OBJECT", 100L,
            "BLESSING_OBJECT", 500L,
            "REDEMPTION_OBJECT", 500L,
            "GEM", 500L,
            "GEM_REMOVER", 500L,
            "BLESSING_BALL", 800L,
            "ITEM_UPGRADER", 500L,
            "SOCKET_STONE", 800L,
            "XP_MULTIPLIER", 500L
    );
    // Configuración de detección de spam
    private static final int MAX_ACTIONS_PER_SECOND = 15;
    private static final int MAX_ACTIONS_PER_5_SECONDS = 40;
    private static final long SPAM_DETECTION_WINDOW = 5000L;
    private static final long CACHE_DURATION = 1000L; // Cache válido por 1 segundo
    private static final int MAX_WARNINGS = 3;
    private static AsyncRateLimiter instance;
    // Mapas thread-safe para operaciones concurrentes
    private final Map<String, AtomicLong> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, ConcurrentLinkedQueue<Long>> playerActionHistory = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> playerWarnings = new ConcurrentHashMap<>();
    // Cache para evitar cálculos repetitivos
    private final Map<UUID, SpamCacheEntry> spamCache = new ConcurrentHashMap<>();
    // Cola para procesamiento asíncrono de logs
    private final ConcurrentLinkedQueue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private final Plugin plugin;

    private AsyncRateLimiter(Plugin plugin) {
        this.plugin = plugin;
        initializeAsyncTasks();
    }

    public static AsyncRateLimiter getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new AsyncRateLimiter(plugin);
        }
        return instance;
    }

    public static AsyncRateLimiter getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AsyncRateLimiter no ha sido inicializado con un plugin");
        }
        return instance;
    }

    /**
     * Verificación SÍNCRONA rápida - solo para cooldowns básicos
     * Ideal para el thread principal del servidor
     */
    public QuickCheckResult quickCheck(Player player, String actionType) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Verificar cooldown básico (muy rápido)
        String key = generateKey(playerId, actionType);
        AtomicLong lastActionTime = playerCooldowns.get(key);

        if (lastActionTime == null) {
            playerCooldowns.put(key, new AtomicLong(currentTime));
            recordActionAsync(playerId, currentTime); // Registro asíncrono
            return new QuickCheckResult(true, 0L);
        }

        long cooldownDuration = COOLDOWN_CONFIG.getOrDefault(actionType, 1000L);
        long timeDifference = currentTime - lastActionTime.get();

        if (timeDifference >= cooldownDuration) {
            lastActionTime.set(currentTime);
            recordActionAsync(playerId, currentTime); // Registro asíncrono
            return new QuickCheckResult(true, 0L);
        }

        long remainingCooldown = cooldownDuration - timeDifference;
        return new QuickCheckResult(false, remainingCooldown);
    }

    /**
     * Verificación ASÍNCRONA completa - incluye detección de spam
     * Se ejecuta en segundo plano
     */
    public CompletableFuture<SpamCheckResult> fullSpamCheckAsync(Player player, String actionType) {
        return CompletableFuture.supplyAsync(() -> {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            // Verificar cache primero
            SpamCacheEntry cached = spamCache.get(playerId);
            if (cached != null && (currentTime - cached.timestamp) < CACHE_DURATION) {
                return cached.result;
            }

            // Realizar verificación completa
            SpamLevel spamLevel = detectSpamPatterns(playerId, currentTime);
            SpamCheckResult result = new SpamCheckResult(true, spamLevel, 0L);

            if (spamLevel != SpamLevel.NONE) {
                result = handleSpamDetection(player, spamLevel);

                // Log asíncrono para spam detectado
                logAsync(LogLevel.WARNING, "Spam detectado", player, spamLevel.name());
            }

            // Actualizar cache
            spamCache.put(playerId, new SpamCacheEntry(result, currentTime));

            return result;
        });
    }

    /**
     * Registro asíncrono de acciones
     */
    private void recordActionAsync(UUID playerId, long timestamp) {
        // Usar CompletableFuture para no bloquear el thread principal
        CompletableFuture.runAsync(() -> {
            ConcurrentLinkedQueue<Long> actions = playerActionHistory.computeIfAbsent(
                    playerId, k -> new ConcurrentLinkedQueue<>()
            );
            actions.offer(timestamp);

            // Limpiar acciones antiguas para mantener memoria bajo control
            while (!actions.isEmpty() &&
                    (timestamp - actions.peek()) > SPAM_DETECTION_WINDOW * 2) {
                actions.poll();
            }
        });
    }

    /**
     * Detección de patrones de spam (optimizada)
     */
    private SpamLevel detectSpamPatterns(UUID playerId, long currentTime) {
        ConcurrentLinkedQueue<Long> actions = playerActionHistory.get(playerId);
        if (actions == null || actions.isEmpty()) {
            return SpamLevel.NONE;
        }

        // Contar acciones usando streams paralelos para mejor rendimiento
        long actionsInLastSecond = actions.parallelStream()
                .mapToLong(time -> currentTime - time)
                .filter(diff -> diff <= 1000L)
                .count();

        long actionsInLast5Seconds = actions.parallelStream()
                .mapToLong(time -> currentTime - time)
                .filter(diff -> diff <= SPAM_DETECTION_WINDOW)
                .count();

        // Determinar nivel de spam
        if (actionsInLastSecond >= MAX_ACTIONS_PER_SECOND) {
            return SpamLevel.CRITICAL;
        }

        if (actionsInLast5Seconds >= MAX_ACTIONS_PER_5_SECONDS) {
            return SpamLevel.HIGH;
        }

        if (actionsInLast5Seconds >= 25) {
            return SpamLevel.MODERATE;
        }

        return SpamLevel.NONE;
    }

    /**
     * Manejo asíncrono de spam detectado
     */
    private SpamCheckResult handleSpamDetection(Player player, SpamLevel spamLevel) {
        UUID playerId = player.getUniqueId();
        AtomicInteger warningsAtomic = playerWarnings.computeIfAbsent(playerId, k -> new AtomicInteger(0));

        int currentWarnings = warningsAtomic.get();

        switch (spamLevel) {
            case CRITICAL:
                // Log crítico asíncrono
                logAsync(LogLevel.CRITICAL, "Spam crítico detectado - posible bot/macro", player, null);
                return new SpamCheckResult(false, SpamLevel.CRITICAL, 0L);

            case HIGH:
                currentWarnings = warningsAtomic.incrementAndGet();
                if (currentWarnings >= MAX_WARNINGS) {
                    logAsync(LogLevel.SEVERE, "Máximo de advertencias alcanzado", player, "Warnings: " + currentWarnings);
                    return new SpamCheckResult(false, SpamLevel.HIGH, 0L);
                }
                break;

            case MODERATE:
                warningsAtomic.compareAndSet(currentWarnings, Math.min(currentWarnings + 1, MAX_WARNINGS - 1));
                break;
        }

        return new SpamCheckResult(false, spamLevel, 0L);
    }

    /**
     * Sistema de logging asíncrono
     */
    private void logAsync(LogLevel level, String message, Player player, String extra) {
        LogEntry entry = new LogEntry(level, message, player.getName(),
                player.getUniqueId(), extra, System.currentTimeMillis());
        logQueue.offer(entry);
    }

    /**
     * Limpieza asíncrona optimizada
     */
    public CompletableFuture<Void> cleanupAsync() {
        return CompletableFuture.runAsync(() -> {
            long currentTime = System.currentTimeMillis();
            long maxCooldown = COOLDOWN_CONFIG.values().stream()
                    .mapToLong(Long::longValue)
                    .max().orElse(1000L);

            // Limpiar cooldowns expirados
            playerCooldowns.entrySet().parallelStream()
                    .filter(entry -> (currentTime - entry.getValue().get()) > maxCooldown * 2)
                    .map(Map.Entry::getKey)
                    .forEach(playerCooldowns::remove);

            // Limpiar historial de acciones
            playerActionHistory.entrySet().parallelStream().forEach(entry -> {
                ConcurrentLinkedQueue<Long> actions = entry.getValue();
                while (!actions.isEmpty() &&
                        (currentTime - actions.peek()) > SPAM_DETECTION_WINDOW * 2) {
                    actions.poll();
                }
            });

            // Limpiar cache de spam expirado
            spamCache.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue().timestamp) > CACHE_DURATION * 10);

            // Limpiar colas vacías
            playerActionHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        });
    }

    /**
     * Inicialización de tareas asíncronas
     */
    private void initializeAsyncTasks() {
        // Tarea de limpieza cada 2 minutos
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupAsync().exceptionally(throwable -> {
                    plugin.getLogger().severe("Error en limpieza asíncrona: " + throwable.getMessage());
                    return null;
                });
            }
        }.runTaskTimerAsynchronously(plugin, 2400L, 2400L); // 2 minutos

        // Procesador de logs asíncrono
        new BukkitRunnable() {
            @Override
            public void run() {
                processLogQueue();
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 100L); // Cada 5 segundos
    }

    /**
     * Procesamiento de cola de logs
     */
    private void processLogQueue() {
        LogEntry entry;
        int processed = 0;

        // Procesar máximo 50 logs por ciclo para evitar lag
        while ((entry = logQueue.poll()) != null && processed < 50) {
            try {
                String logMessage = String.format("[ANTI-SPAM] %s - %s (UUID: %s) %s",
                        entry.message, entry.playerName, entry.playerId,
                        entry.extra != null ? "- " + entry.extra : "");

                switch (entry.level) {
                    case CRITICAL:
                    case SEVERE:
                        plugin.getLogger().severe(logMessage);
                        break;
                    case WARNING:
                        plugin.getLogger().warning(logMessage);
                        break;
                    case INFO:
                        plugin.getLogger().info(logMessage);
                        break;
                }

                processed++;
            } catch (Exception e) {
                plugin.getLogger().severe("Error procesando log: " + e.getMessage());
            }
        }
    }

    /**
     * Limpieza de datos de jugador (thread-safe)
     */
    public void clearPlayerDataAsync(Player player) {
        CompletableFuture.runAsync(() -> {
            UUID playerId = player.getUniqueId();
            String playerPrefix = playerId.toString() + ":";

            // Limpiar cooldowns
            playerCooldowns.entrySet().removeIf(entry ->
                    entry.getKey().startsWith(playerPrefix));

            // Limpiar historial
            playerActionHistory.remove(playerId);

            // Limpiar advertencias
            playerWarnings.remove(playerId);

            // Limpiar cache
            spamCache.remove(playerId);
        });
    }

    // Métodos de utilidad thread-safe
    public int getPlayerWarnings(Player player) {
        AtomicInteger warnings = playerWarnings.get(player.getUniqueId());
        return warnings != null ? warnings.get() : 0;
    }

    public void resetPlayerWarnings(Player player) {
        playerWarnings.remove(player.getUniqueId());
        spamCache.remove(player.getUniqueId()); // Limpiar cache también
    }

    private String generateKey(UUID playerId, String actionType) {
        return playerId.toString() + ":" + actionType;
    }

    public enum SpamLevel {
        NONE, MODERATE, HIGH, CRITICAL
    }

    private enum LogLevel {
        INFO, WARNING, SEVERE, CRITICAL
    }

    // Clases de datos
    public static class QuickCheckResult {
        private final boolean allowed;
        private final long remainingCooldown;

        public QuickCheckResult(boolean allowed, long remainingCooldown) {
            this.allowed = allowed;
            this.remainingCooldown = remainingCooldown;
        }

        public boolean isAllowed() { return allowed; }
        public long getRemainingCooldown() { return remainingCooldown; }
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

    private static class SpamCacheEntry {
        final SpamCheckResult result;
        final long timestamp;

        SpamCacheEntry(SpamCheckResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
    }

    private static class LogEntry {
        final LogLevel level;
        final String message;
        final String playerName;
        final UUID playerId;
        final String extra;
        final long timestamp;

        LogEntry(LogLevel level, String message, String playerName,
                 UUID playerId, String extra, long timestamp) {
            this.level = level;
            this.message = message;
            this.playerName = playerName;
            this.playerId = playerId;
            this.extra = extra;
            this.timestamp = timestamp;
        }
    }
}