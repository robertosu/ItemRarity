package cl.nightcore.itemrarity.loot;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import org.bukkit.entity.Player;

public class LevelGapManager {
    
    // Configuración del sistema de diferencia de niveles
    private static final int MAX_LEVEL_DIFFERENCE = 50; // Máxima diferencia permitida
    private static final boolean HIGHER_LEVEL_PENALTY = true; // Si penalizar jugadores de nivel muy alto
    private static final boolean LOWER_LEVEL_PENALTY = false; // Si penalizar jugadores de nivel muy bajo
    
    /**
     * Verifica si el drop debe ser cancelado por diferencia de niveles
     * @param player El jugador que mató al mob
     * @param mobLevel El nivel del mob
     * @return true si el drop debe ser cancelado
     */
    public static boolean shouldCancelDrop(Player player, int mobLevel) {
        int playerLevel = MythicProjectiles.getPlayerLevel(player);
        int levelDifference = playerLevel - mobLevel;
        
        // Jugador de nivel muy alto vs mob de nivel bajo
        if (HIGHER_LEVEL_PENALTY && levelDifference > MAX_LEVEL_DIFFERENCE) {
            return true;
        }
        
        // Jugador de nivel muy bajo vs mob de nivel alto
        if (LOWER_LEVEL_PENALTY && levelDifference < -MAX_LEVEL_DIFFERENCE) {
            return true;
        }
        
        return false;
    }



    public static double getLevelPenaltyMultiplier(Player player, int mobLevel) {
        int playerLevel = MythicProjectiles.getPlayerLevel(player);
        int levelDifference = Math.abs(playerLevel - mobLevel);

        if (levelDifference <= 10) return 1.0;       // 100%
        else if (levelDifference <= 20) return 0.7;  // 70%
        else if (levelDifference <= 30) return 0.5;  // 50%
        else if (levelDifference <= 40) return 0.3;  // 30%
        else if (levelDifference <= MAX_LEVEL_DIFFERENCE) return 0.1; // 10%
        else return 0.0;
    }
    
    /**
     * Versión configurable del sistema
     */
    public static boolean shouldCancelDrop(Player player, int mobLevel, LevelGapConfig config) {
        int playerLevel = MythicProjectiles.getPlayerLevel(player);
        int levelDifference = playerLevel - mobLevel;
        
        if (config.higherLevelPenalty && levelDifference > config.maxLevelDifference) {
            return true;
        }
        
        if (config.lowerLevelPenalty && levelDifference < -config.maxLevelDifference) {
            return true;
        }
        
        return false;
    }
    
    public record LevelGapConfig(int maxLevelDifference, boolean higherLevelPenalty, boolean lowerLevelPenalty) {
        public static LevelGapConfig defaultConfig() {
            return new LevelGapConfig(10, true, false);
        }
        
        public static LevelGapConfig strictConfig() {
            return new LevelGapConfig(5, true, true);
        }
        
        public static LevelGapConfig lenientConfig() {
            return new LevelGapConfig(20, false, false);
        }
    }
}