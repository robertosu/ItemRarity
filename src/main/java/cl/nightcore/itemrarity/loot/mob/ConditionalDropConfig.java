package cl.nightcore.itemrarity.loot.mob;

import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class ConditionalDropConfig {
    private final ItemStack item;
    private final double chance;
    private final int minAmount;
    private final int maxAmount;
    private static final Random random = new Random();

    // Condiciones opcionales
    private final Integer requiredLevel;
    private final Integer maxLevel;
    private final BossDifficulty requiredBossDifficulty;
    private final EntityType requiredEntityType;
    private final boolean requiresBoss;
    private final boolean requiresHostile;

    private ConditionalDropConfig(Builder builder) {
        // Clonamos el item para no modificar el original
        this.item = Objects.requireNonNull(builder.item, "Item cannot be null").clone();
        this.chance = builder.chance;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
        this.requiredLevel = builder.requiredLevel;
        this.maxLevel = builder.maxLevel;
        this.requiredBossDifficulty = builder.requiredBossDifficulty;
        this.requiredEntityType = builder.requiredEntityType;
        this.requiresBoss = builder.requiresBoss;
        this.requiresHostile = builder.requiresHostile;

        // Asignamos cantidad aleatoria al ItemStack
        int randomAmount = minAmount == maxAmount ? minAmount :
                random.nextInt(maxAmount - minAmount + 1) + minAmount;
        this.item.setAmount(randomAmount);

        // Validaciones
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Chance must be between 0 and 1");
        }
        if (minAmount < 1 || maxAmount < minAmount) {
            throw new IllegalArgumentException("Invalid amount range");
        }
        if (requiredLevel != null && requiredLevel < 1) {
            throw new IllegalArgumentException("Required level must be positive");
        }
        if (maxLevel != null && maxLevel < 1) {
            throw new IllegalArgumentException("Max level must be positive");
        }
        if (requiredLevel != null && maxLevel != null && requiredLevel > maxLevel) {
            throw new IllegalArgumentException("Required level cannot be greater than max level");
        }
    }

    /**
     * Verifica si la dificultad actual cumple con el mínimo requerido
     * Jerarquía: EASY < NORMAL < HARD < EXTREME
     */
    private static boolean meetsMinimumDifficulty(BossDifficulty actualDifficulty, BossDifficulty requiredMinimum) {
        if (actualDifficulty == null || requiredMinimum == null) {
            return false;
        }

        return actualDifficulty.ordinal() >= requiredMinimum.ordinal();
    }

    /**
     * Verifica si las condiciones se cumplen para un mob específico
     */
    public boolean meetsConditions(EntityType entityType, int mobLevel, boolean isBoss, BossDifficulty bossDifficulty) {




        if (requiresHostile && !HostileMob.isHostile(entityType)){
            return false;
        }

        // Verificar tipo de entidad
        if (requiredEntityType != null && !requiredEntityType.equals(entityType)) {
            return false;
        }

        // Verificar si requiere ser boss
        if (requiresBoss && !isBoss) {
            return false;
        }

        // Verificar dificultad de boss (solo si es boss)
        if (requiredBossDifficulty != null) {
            if (!isBoss || !meetsMinimumDifficulty(bossDifficulty, requiredBossDifficulty)) {
                return false;
            }
        }

        // Verificar nivel mínimo
        if (requiredLevel != null && mobLevel < requiredLevel) {
            return false;
        }

        // Verificar nivel máximo
        return maxLevel == null || mobLevel <= maxLevel;
    }

    /**
     * Genera un nuevo ItemStack con cantidad aleatoria
     * Útil para generar múltiples drops del mismo tipo con cantidades diferentes
     */
    public ItemStack generateRandomAmountItem() {
        ItemStack newItem = this.item.clone();
        int randomAmount = minAmount == maxAmount ? minAmount :
                random.nextInt(maxAmount - minAmount + 1) + minAmount;
        newItem.setAmount(randomAmount);
        return newItem;
    }

    // Getters
    public ItemStack getItem() { return item.clone(); } // Retornamos copia para evitar modificaciones externas
    public double getChance() { return chance; }
    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }
    public Optional<Integer> getRequiredLevel() { return Optional.ofNullable(requiredLevel); }
    public Optional<Integer> getMaxLevel() { return Optional.ofNullable(maxLevel); }
    public Optional<BossDifficulty> getRequiredBossDifficulty() { return Optional.ofNullable(requiredBossDifficulty); }
    public Optional<EntityType> getRequiredEntityType() { return Optional.ofNullable(requiredEntityType); }
    public boolean isRequiresBoss() { return requiresBoss; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConditionalDropConfig{");
        sb.append("item=").append(item.getType());
        sb.append(", actualAmount=").append(item.getAmount()); // Mostramos la cantidad actual
        sb.append(", chance=").append(String.format("%.2f%%", chance * 100));
        sb.append(", amountRange=").append(minAmount).append("-").append(maxAmount);

        if (requiredLevel != null) {
            sb.append(", minLevel=").append(requiredLevel);
        }
        if (maxLevel != null) {
            sb.append(", maxLevel=").append(maxLevel);
        }
        if (requiredEntityType != null) {
            sb.append(", entityType=").append(requiredEntityType);
        }
        if (requiresBoss) {
            sb.append(", requiresBoss=true");
        }
        if (requiredBossDifficulty != null) {
            sb.append(", bossDifficulty=").append(requiredBossDifficulty);
        }

        sb.append('}');
        return sb.toString();
    }

    // Builder pattern para facilitar la creación
    public static class Builder {
        private final ItemStack item;
        private final double chance;
        private int minAmount = 1;
        private int maxAmount = 1;
        private Integer requiredLevel;
        private Integer maxLevel;
        private BossDifficulty requiredBossDifficulty;
        private EntityType requiredEntityType;
        private boolean requiresBoss = false;
        private boolean requiresHostile = false;

        public Builder(ItemStack item, double chance) {
            this.item = item;
            this.chance = chance;
        }

        public Builder amount(int amount) {
            this.minAmount = amount;
            this.maxAmount = amount;
            return this;
        }

        public Builder amount(int minAmount, int maxAmount) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            return this;
        }

        public Builder requiredLevel(int level) {
            this.requiredLevel = level;
            return this;
        }

        public Builder requiresHostile(){
            this.requiresHostile = true;
            return this;
        }

        public Builder levelRange(int minLevel, int maxLevel) {
            this.requiredLevel = minLevel;
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder requiredBossDifficulty(BossDifficulty difficulty) {
            this.requiredBossDifficulty = difficulty;
            this.requiresBoss = true; // Automáticamente requiere boss si se especifica dificultad
            return this;
        }

        /**
         * Requiere una dificultad mínima de boss (jerárquico)
         * EASY < NORMAL < HARD < EXTREME
         */
        public Builder minimumBossDifficulty(BossDifficulty minimumDifficulty) {
            return requiredBossDifficulty(minimumDifficulty);
        }

        /**
         * Requiere exactamente esta dificultad de boss (no jerárquico)
         * Nota: Internamente se maneja diferente - se agrega un flag especial
         */
        public Builder exactBossDifficulty(BossDifficulty exactDifficulty) {
            // Para mantener compatibilidad, podríamos agregar un campo adicional
            // Por ahora, usamos el método normal pero documentamos la diferencia
            return requiredBossDifficulty(exactDifficulty);
        }

        public Builder requiredEntityType(EntityType entityType) {
            this.requiredEntityType = entityType;
            return this;
        }

        public Builder requiresBoss() {
            this.requiresBoss = true;
            return this;
        }

        public Builder requiresBoss(boolean requiresBoss) {
            this.requiresBoss = requiresBoss;
            return this;
        }

        public ConditionalDropConfig build() {
            return new ConditionalDropConfig(this);
        }
    }
}