// PartyManager.java - Sistema mejorado con soporte para mobs normales
package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PartyManager implements Listener {

    // Maps principales (thread-safe)
    private static final Map<UUID, Party> playerParties = new ConcurrentHashMap<>();
    private static final Map<UUID, Party> allParties = new ConcurrentHashMap<>();

    // Sistema de invitaciones
    private static final Map<UUID, PendingInvitation> pendingInvitations = new ConcurrentHashMap<>();

    // Lock para operaciones críticas
    private static final ReentrantLock OPERATION_LOCK = new ReentrantLock();
    // ConfiguraciÃ³n
    private static final int MAX_PARTY_SIZE = 6;
    private static final double PARTY_EXP_BONUS_PER_MEMBER = 0.15; // 15% por miembro adicional
    private static final double MAX_PARTY_BONUS = 0.6; // MÃ¡ximo 60% bonus
    private static final long INVITATION_TIMEOUT = 30000; // 30 segundos
    private static final long CLEANUP_INTERVAL = 60000; // 1 minuto
    // ConfiguraciÃ³n para mobs normales
    private static final boolean NORMAL_MOBS_USE_PARTY = true; // Activar/desactivar party para mobs normales
    // 🔥 NUEVA CONFIGURACIÓN: Distancia máxima para recibir rewards
    public static final double MAX_DISTANCE_FOR_REWARDS = 50.0; // bloques
    public static final boolean DISTANCE_CHECK_ENABLED = true;
    // Tareas de limpieza
    private static BukkitTask cleanupTask;
    private static BukkitTask invitationCleanupTask;

    // InicializaciÃ³n
    static {
        startCleanupTasks();
    }

    // ================ INTERFAZ PÃšBLICA PRINCIPAL ================

    /**
     * API principal para crear una party
     */
    public static PartyResult createParty(Player leader) {
        if (leader == null || !leader.isOnline()) {
            return PartyResult.error("Jugador no válido o offline");
        }

        OPERATION_LOCK.lock();
        try {
            if (isInParty(leader.getUniqueId())) {
                return PartyResult.error("Ya estás en una party");
            }

            UUID partyId = UUID.randomUUID();
            Party party = new Party(partyId, leader.getUniqueId());

            allParties.put(partyId, party);
            playerParties.put(leader.getUniqueId(), party);

            return PartyResult.success("Party creada exitosamente", party);

        } finally {
            OPERATION_LOCK.unlock();
        }
    }

    /**
     * API principal para invitar jugador
     */
    public static PartyResult invitePlayer(Player leader, Player target) {
        if (leader == null || target == null) {
            return PartyResult.error("Jugadores no válidos");
        }

        if (!leader.isOnline() || !target.isOnline()) {
            return PartyResult.error("Jugadores deben estar online");
        }

        OPERATION_LOCK.lock();
        try {
            Party party = playerParties.get(leader.getUniqueId());
            if (party == null) {
                return PartyResult.error("No estás en una party");
            }

            if (!party.isLeader(leader.getUniqueId())) {
                return PartyResult.error("Solo el líder puede invitar");
            }

            if (party.getMembers().size() >= MAX_PARTY_SIZE) {
                return PartyResult.error("Party llena (máximo " + MAX_PARTY_SIZE + " jugadores)");
            }

            if (isInParty(target.getUniqueId())) {
                return PartyResult.error(target.getName() + " ya estás en una party");
            }

            if (hasPendingInvitation(target.getUniqueId())) {
                return PartyResult.error(target.getName() + " ya tiene una invitación pendiente");
            }

            // Crear invitaciÃ³n
            PendingInvitation invitation = new PendingInvitation(
                    party.getId(),
                    leader.getUniqueId(),
                    target.getUniqueId(),
                    System.currentTimeMillis() + INVITATION_TIMEOUT
            );

            pendingInvitations.put(target.getUniqueId(), invitation);

            return PartyResult.success("Invitación enviada a " + target.getName(), party);

        } finally {
            OPERATION_LOCK.unlock();
        }
    }

    /**
     * API principal para aceptar invitaciÃ³n
     */
    public static PartyResult acceptInvitation(Player player) {
        if (player == null || !player.isOnline()) {
            return PartyResult.error("Jugador no válido");
        }

        OPERATION_LOCK.lock();
        try {
            PendingInvitation invitation = pendingInvitations.remove(player.getUniqueId());
            if (invitation == null) {
                return PartyResult.error("No tienes invitaciones pendientes");
            }

            if (invitation.isExpired()) {
                return PartyResult.error("La invitaciÃ³n ha expirado");
            }

            if (isInParty(player.getUniqueId())) {
                return PartyResult.error("Ya estÃ¡s en una party");
            }

            Party party = allParties.get(invitation.partyId());
            if (party == null) {
                return PartyResult.error("La party ya no existe");
            }

            if (party.getMembers().size() >= MAX_PARTY_SIZE) {
                return PartyResult.error("La party estÃ¡ llena");
            }

            // Agregar a la party
            party.addMember(player.getUniqueId());
            playerParties.put(player.getUniqueId(), party);

            return PartyResult.success("Te has unido a la party", party);

        } finally {
            OPERATION_LOCK.unlock();
        }
    }

    /**
     * API principal para rechazar invitaciÃ³n
     */
    public static PartyResult rejectInvitation(Player player) {
        if (player == null) {
            return PartyResult.error("Jugador no vÃ¡lido");
        }

        PendingInvitation invitation = pendingInvitations.remove(player.getUniqueId());
        if (invitation == null) {
            return PartyResult.error("No tienes invitaciones pendientes");
        }

        return PartyResult.success("InvitaciÃ³n rechazada");
    }

    /**
     * API principal para salir de party
     */
    public static PartyResult leaveParty(Player player) {
        return leaveParty(player.getUniqueId());
    }

    /**
     * API interna para salir de party (usada por eventos)
     */
    public static PartyResult leaveParty(UUID playerId) {
        OPERATION_LOCK.lock();
        try {
            Party party = playerParties.get(playerId);
            if (party == null) {
                return PartyResult.error("No estÃ¡s en una party");
            }

            // Remover de ambos maps de forma atÃ³mica
            playerParties.remove(playerId);
            party.removeMember(playerId);

            if (party.getMembers().isEmpty()) {
                // Party vacÃ­a, disolverla
                allParties.remove(party.getId());
                return PartyResult.success("Party disuelta");
            } else if (party.isLeader(playerId)) {
                // Transferir liderazgo
                UUID newLeader = findNewLeader(party);
                if (newLeader != null) {
                    party.setLeader(newLeader);
                    return PartyResult.success("Has salido de la party. Nuevo lÃ­der: " +
                            getPlayerName(newLeader));
                } else {
                    // No hay lÃ­der vÃ¡lido, disolver party
                    dissolveParty(party.getId());
                    return PartyResult.success("Party disuelta - no habÃ­a miembros online");
                }
            }

            return PartyResult.success("Has salido de la party");

        } finally {
            OPERATION_LOCK.unlock();
        }
    }

    /**
     * API principal para disolver party (solo lÃ­der)
     */
    public static PartyResult dissolveParty(Player leader) {
        if (leader == null || !leader.isOnline()) {
            return PartyResult.error("Jugador no válido");
        }

        OPERATION_LOCK.lock();
        try {
            Party party = playerParties.get(leader.getUniqueId());
            if (party == null) {
                return PartyResult.error("No estás en una party");
            }

            if (!party.isLeader(leader.getUniqueId())) {
                return PartyResult.error("Solo el líder puede disolver la party");
            }

            dissolveParty(party.getId());
            return PartyResult.success("Party disuelta");

        } finally {
            OPERATION_LOCK.unlock();
        }
    }


    // ================ INTERFAZ PARA SISTEMA DE DROPS ================

    /**
     * Obtiene miembros de party online para sistema de drops de BOSSES Ãºnicamente
     */
    public static Set<Player> getPartyMembersForBoss(Player player) {
        Party party = playerParties.get(player.getUniqueId());
        if (party == null) {
            return Set.of(player); // Solo el jugador si no estÃ¡ en party
        }

        return party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toSet());
    }

    /**
     * Para mobs normales - ahora tambiÃ©n usa party si estÃ¡ habilitado
     */
    public static Set<Player> getPartyMembersForNormalMob(Player killer) {
        if (!NORMAL_MOBS_USE_PARTY) {
            return Set.of(killer); // Solo el killer si las parties estÃ¡n deshabilitadas para mobs normales
        }

        Party party = playerParties.get(killer.getUniqueId());
        if (party == null) {
            return Set.of(killer); // Solo el killer si no estÃ¡ en party
        }

        // Devolver todos los miembros online de la party
        Set<Player> partyMembers = party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toSet());

        // Asegurar que el killer estÃ© incluido
        partyMembers.add(killer);

        return partyMembers;
    }

    /**
     * Verifica si un jugador estÃ¡ en party
     */
    public static boolean isInParty(UUID playerId) {
        return playerParties.containsKey(playerId);
    }

    /**
     * Obtiene la party de un jugador
     */
    public static Optional<Party> getPlayerParty(UUID playerId) {
        return Optional.ofNullable(playerParties.get(playerId));
    }

    /**
     * Calcula bonus de experiencia por party - MEJORADO
     */
    public static double getPartyExpBonus(UUID playerId) {
        Party party = playerParties.get(playerId);
        if (party == null) {
            return 1.0; // Sin bonus si no estÃ¡ en party
        }

        int onlineMembers = party.getOnlineMemberCount();
        if (onlineMembers <= 1) {
            return 1.0; // Sin bonus si estÃ¡ solo
        }

        // Calcular bonus: 15% por cada miembro adicional (despuÃ©s del primero)
        return 1.0 + Math.min((onlineMembers - 1) * PARTY_EXP_BONUS_PER_MEMBER, MAX_PARTY_BONUS);
    }

    /**
     * ConfiguraciÃ³n para habilitar/deshabilitar parties en mobs normales
     */
    public static boolean isNormalMobPartyEnabled() {
        return NORMAL_MOBS_USE_PARTY;
    }

    // ================ MÃ‰TODOS INTERNOS ================

    private static void dissolveParty(UUID partyId) {
        Party party = allParties.remove(partyId);
        if (party != null) {
            // Remover todos los miembros
            for (UUID memberId : new HashSet<>(party.getMembers())) {
                playerParties.remove(memberId);
            }
            party.getMembers().clear();
        }
    }

    private static UUID findNewLeader(Party party) {
        return party.getMembers().stream()
                .filter(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    return p != null && p.isOnline();
                })
                .findFirst()
                .orElse(null);
    }

    private static String getPlayerName(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null ? player.getName() : "Unknown";
    }

    private static boolean hasPendingInvitation(UUID playerId) {
        PendingInvitation invitation = pendingInvitations.get(playerId);
        return invitation != null && !invitation.isExpired();
    }

    // ================ SISTEMA DE LIMPIEZA ================

    private static void startCleanupTasks() {
        // Limpieza general cada minuto
        cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                ItemRarity.PLUGIN,
                PartyManager::performCleanup,
                CLEANUP_INTERVAL / 50,
                CLEANUP_INTERVAL / 50
        );

        // Limpieza de invitaciones cada 10 segundos
        invitationCleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                ItemRarity.PLUGIN,
                PartyManager::cleanupExpiredInvitations,
                200L, // 10 segundos
                200L
        );
    }

    private static void performCleanup() {
        Set<UUID> playersToRemove = new HashSet<>();

        // Encontrar jugadores offline
        for (Map.Entry<UUID, Party> entry : playerParties.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                playersToRemove.add(entry.getKey());
            }
        }

        // Remover en el hilo principal
        if (!playersToRemove.isEmpty()) {
            Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                for (UUID playerId : playersToRemove) {
                    leaveParty(playerId);
                }

                if (!playersToRemove.isEmpty()) {
                    ItemRarity.PLUGIN.getLogger().info(
                            "Cleaned up " + playersToRemove.size() + " offline players from parties");
                }
            });
        }
    }

    private static void cleanupExpiredInvitations() {
        Set<UUID> expiredInvitations = pendingInvitations.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (UUID playerId : expiredInvitations) {
            pendingInvitations.remove(playerId);
        }

        if (!expiredInvitations.isEmpty()) {
            ItemRarity.PLUGIN.getLogger().fine(
                    "Cleaned up " + expiredInvitations.size() + " expired invitations");
        }
    }

    // ================ EVENT LISTENERS ================

    public static PartyStats getStats() {
        return new PartyStats(
                allParties.size(),
                playerParties.size(),
                pendingInvitations.size(),
                allParties.values().stream()
                        .mapToInt(Party::getSize)
                        .average()
                        .orElse(0.0)
        );
    }

    public static List<Component> getPartyInfo(Player player) {
        List<Component> info = new ArrayList<>();


        Party party = playerParties.get(player.getUniqueId());
        if (party == null) {
            info.add(Component.text("No estás en ninguna party"));
            return info;
        }

        info.add(Component.text("=== INFORMACIÃ“N DE PARTY ==="));
        info.add(Component.text("ID: " + party.getId().toString().substring(0, 8) + "..."));
        info.add(Component.text("Líder: " + getPlayerName(party.getLeaderId())));
        info.add(Component.text("Miembros (" + party.getSize() + "/" + MAX_PARTY_SIZE + "):"));

        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            String status = (member != null && member.isOnline()) ? "§aOnline" : "§cOffline";
            String leader = party.isLeader(memberId) ? " §e[Líder]" : "";
            info.add(Component.text("  - " + getPlayerName(memberId) + " " + status + leader));
        }

        double bonus = getPartyExpBonus(player.getUniqueId());
        info.add(Component.text("Bonus EXP: +" + Math.round((bonus - 1.0) * 100) + "%"));
        info.add(Component.text("Mobs normales: " + (NORMAL_MOBS_USE_PARTY ? "§aHabilitado" : "§cDeshabilitado")));
        info.add(Component.text("Creada: " + new Date(party.getCreatedTime())));

        return info;
    }

    // ================ MÃ‰TODOS DE UTILIDAD/DEBUG ================

    public static void shutdown() {
        if (cleanupTask != null) cleanupTask.cancel();
        if (invitationCleanupTask != null) invitationCleanupTask.cancel();

        allParties.clear();
        playerParties.clear();
        pendingInvitations.clear();
    }

    /**
     * 🔥 NUEVO: Verifica si un jugador está dentro del rango de distancia permitido
     */
    public static boolean isWithinRewardDistance(Player player, Location deathLocation) {
        if (!DISTANCE_CHECK_ENABLED) {
            return true; // Si está deshabilitada, todos pasan
        }

        if (player == null || !player.isOnline() || deathLocation == null) {
            return false;
        }

        Location playerLocation = player.getLocation();

        // Verificar que estén en el mismo mundo
        if (!playerLocation.getWorld().equals(deathLocation.getWorld())) {
            return false;
        }

        double distance = playerLocation.distance(deathLocation);
        return distance <= MAX_DISTANCE_FOR_REWARDS;
    }

    /**
     * 🔥 NUEVO: Filtra jugadores por distancia
     */
    public static Set<Player> filterPlayersByDistance(Set<Player> players, Location deathLocation) {
        if (!DISTANCE_CHECK_ENABLED) {
            return players; // Si está deshabilitada, devolver todos
        }

        return players.stream()
                .filter(player -> isWithinRewardDistance(player, deathLocation))
                .collect(Collectors.toSet());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        leaveParty(event.getPlayer().getUniqueId());
        pendingInvitations.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        leaveParty(event.getPlayer().getUniqueId());
        pendingInvitations.remove(event.getPlayer().getUniqueId());
    }

    // ================ CLASES DE DATOS ================

    public static class Party {
        private final UUID id;
        private final Set<UUID> members = ConcurrentHashMap.newKeySet();
        private final long createdTime;
        private volatile UUID leaderId;

        public Party(UUID id, UUID leaderId) {
            this.id = id;
            this.leaderId = leaderId;
            this.members.add(leaderId);
            this.createdTime = System.currentTimeMillis();
        }

        public void addMember(UUID playerId) {
            members.add(playerId);
        }

        public void removeMember(UUID playerId) {
            members.remove(playerId);
        }

        public boolean isLeader(UUID playerId) {
            return leaderId.equals(playerId);
        }

        public void setLeader(UUID newLeaderId) {
            if (members.contains(newLeaderId)) {
                this.leaderId = newLeaderId;
            }
        }

        public int getOnlineMemberCount() {
            return (int) members.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(Player::isOnline)
                    .count();
        }

        // Getters
        public UUID getId() { return id; }
        public UUID getLeaderId() { return leaderId; }
        public Set<UUID> getMembers() { return new HashSet<>(members); }
        public long getCreatedTime() { return createdTime; }
        public int getSize() { return members.size(); }
    }

    public record PendingInvitation(UUID partyId, UUID leaderId, UUID targetId, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public static class PartyResult {
        private final boolean success;
        private final String message;
        private final Party party;

        private PartyResult(boolean success, String message, Party party) {
            this.success = success;
            this.message = message;
            this.party = party;
        }

        public static PartyResult success(String message) {
            return new PartyResult(true, message, null);
        }

        public static PartyResult success(String message, Party party) {
            return new PartyResult(true, message, party);
        }

        public static PartyResult error(String message) {
            return new PartyResult(false, message, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Optional<Party> getParty() { return Optional.ofNullable(party); }
    }

    public record PartyStats(int totalParties, int totalPlayers, int pendingInvitations, double averageSize) {
        @Override
        public String toString() {
            return String.format("PartyStats{parties: %d, players: %d, invites: %d, avgSize: %.1f}",
                    totalParties, totalPlayers, pendingInvitations, averageSize);
        }
    }
}