package cl.nightcore.itemrarity.loot.mob;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import cl.nightcore.itemrarity.loot.Items;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;

import java.util.ArrayList;
import java.util.List;

public class MobDrops {

    private static MobDrops instance;
    private final List<ConditionalDropConfig> conditionalDrops = new ArrayList<>();

    private MobDrops() {
        addMagicObjects();
        addBlessingObjects();
        addRedemptionObjects();

        addGems();
        addPotions();

        addUpgraders();
        addXpMultipliers();

        addGemRemovers();
        addOtherObjects();
    }

    public static synchronized MobDrops instance() {
        if (instance == null) {
            instance = new MobDrops();
        }
        return instance;
    }

    public List<ConditionalDropConfig> getMobDrops(){
        return conditionalDrops;
    }

    private void addMagicObjects(){
        // ===== MAGIC OBJECT - Base drops =====
        // EASY DIFFICULTY (Activado por EASY+, NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.30)
                .amount(1, 3).levelRange(1,20).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.25)
                .amount(2, 4).levelRange(21,40).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.20)
                .amount(3, 5).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.18)
                .amount(4, 6).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.15)
                .amount(5, 7).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EASY).build());

        // NORMAL DIFFICULTY (Activado por NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.25)
                .amount(2, 4).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.22)
                .amount(3, 5).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.20)
                .amount(4, 6).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.18)
                .amount(5, 7).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.16)
                .amount(6, 8).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        // HARD DIFFICULTY (Activado por HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.20)
                .amount(3, 5).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.18)
                .amount(4, 6).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.16)
                .amount(5, 7).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.14)
                .amount(6, 9).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.12)
                .amount(7, 10).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.HARD).build());

        // EXTREME DIFFICULTY (Activado solo por EXTREME)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.18)
                .amount(4, 7).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.16)
                .amount(5, 8).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.14)
                .amount(6, 9).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.12)
                .amount(7, 11).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getMagicObject(), 0.10)
                .amount(9, 15).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EXTREME).build());
    }

    private void addRedemptionObjects(){
        // ===== REDEMPTION OBJECT - 5x más raro que Magic Object =====
        // EASY DIFFICULTY (Activado por EASY+, NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.06)
                .amount(1, 2).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.05)
                .amount(1, 2).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.04)
                .amount(1, 3).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.036)
                .amount(2, 3).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.03)
                .amount(2, 4).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EASY).build());

        // NORMAL DIFFICULTY (Activado por NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.05)
                .amount(1, 2).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.044)
                .amount(1, 3).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.04)
                .amount(2, 3).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.036)
                .amount(2, 4).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.032)
                .amount(3, 4).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        // HARD DIFFICULTY (Activado por HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.04)
                .amount(1, 3).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.036)
                .amount(2, 3).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.032)
                .amount(2, 4).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.028)
                .amount(3, 5).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.024)
                .amount(3, 6).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.HARD).build());

        // EXTREME DIFFICULTY (Activado solo por EXTREME)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.036)
                .amount(2, 4).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.032)
                .amount(2, 4).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.028)
                .amount(3, 5).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.024)
                .amount(3, 6).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getRedemptionObject(), 0.02)
                .amount(4, 8).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EXTREME).build());
    }


    private void addBlessingObjects(){
        // ===== BLESSING OBJECT - 5x más raro que Magic Object =====
        // EASY DIFFICULTY (Activado por EASY+, NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.06)
                .amount(1, 2).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.05)
                .amount(1, 2).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.04)
                .amount(1, 3).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.036)
                .amount(2, 3).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EASY).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.03)
                .amount(2, 4).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EASY).build());

        // NORMAL DIFFICULTY (Activado por NORMAL+, HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.05)
                .amount(1, 2).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.044)
                .amount(1, 3).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.04)
                .amount(2, 3).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.036)
                .amount(2, 4).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.032)
                .amount(3, 4).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        // HARD DIFFICULTY (Activado por HARD+, EXTREME+)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.04)
                .amount(1, 3).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.036)
                .amount(2, 3).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.032)
                .amount(2, 4).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.028)
                .amount(3, 5).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.HARD).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.024)
                .amount(3, 6).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.HARD).build());

        // EXTREME DIFFICULTY (Activado solo por EXTREME)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.036)
                .amount(2, 4).levelRange(1, 20).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.032)
                .amount(2, 4).levelRange(21, 40).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.028)
                .amount(3, 5).levelRange(41, 60).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.024)
                .amount(3, 6).levelRange(61, 80).minimumBossDifficulty(BossDifficulty.EXTREME).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingObject(), 0.02)
                .amount(4, 8).levelRange(81, 100).minimumBossDifficulty(BossDifficulty.EXTREME).build());

    }

    private void addGems() {
        // Recorre cada stat solo una vez y crea todos sus drops de gemas
        for (CombinedStats stat : CombinedStats.values()) {
            // ===== GEMAS NIVEL 1 =====
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getGem(stat, 1), 0.1)
                            .minimumBossDifficulty(BossDifficulty.EASY)
                            .levelRange(1, 20)
                            .build());
            // ===== GEMAS NIVEL 2 =====
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getGem(stat, 2), 0.1)
                            .minimumBossDifficulty(BossDifficulty.NORMAL)
                            .levelRange(21, 40)
                            .build());
            // ===== GEMAS NIVEL 3 =====
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getGem(stat, 3), 0.1)
                            .minimumBossDifficulty(BossDifficulty.HARD)
                            .levelRange(41, 60)
                            .build());
            // ===== GEMAS NIVEL 4 =====
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getGem(stat, 4), 0.1)
                            .minimumBossDifficulty(BossDifficulty.HARD)
                            .levelRange(61, 80)
                            .build());
            // ===== GEMAS NIVEL 5 =====
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getGem(stat, 5), 0.1)
                            .minimumBossDifficulty(BossDifficulty.EXTREME)
                            .levelRange(81, 100)
                            .build());
        }
    }

    private void addPotions() {
        // Recorre cada stat solo una vez y crea todos sus drops
        for (CombinedStats stat : CombinedStats.values()) {
            // ===== NIVEL 1 =====
            // LVL 1 LOW POTIONS
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.ONE, PotionManager.PotionDuration.LOW),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(1,20).build());

            // LVL 1 MEDIUM POTIONS
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.ONE, PotionManager.PotionDuration.MEDIUM),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(5,20).build());

            // LVL 1 HIGH POTIONS
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.ONE, PotionManager.PotionDuration.HIGH),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(10,20).build());

            // LVL 1 HIGHEST POTIONS
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.ONE, PotionManager.PotionDuration.HIGHEST),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(15,20).build());

            // ===== NIVEL 2 =====
            // LVL 2 LOW POTIONS
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.TWO, PotionManager.PotionDuration.LOW),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(21,40).build());

            // LVL 2 MEDIUM
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.TWO, PotionManager.PotionDuration.MEDIUM),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(25,40).build());

            // LVL 2 HIGH
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.TWO, PotionManager.PotionDuration.HIGH),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(30,40).build());

            // LVL 2 HIGHEST
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.TWO, PotionManager.PotionDuration.HIGHEST),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(35,40).build());

            // ===== NIVEL 3 =====
            // LVL 3 LOW
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.THREE, PotionManager.PotionDuration.LOW),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(41,60).build());

            // LVL 3 MEDIUM
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.THREE, PotionManager.PotionDuration.MEDIUM),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(45,60).build());

            // LVL 3 HIGH
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.THREE, PotionManager.PotionDuration.HIGH),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(50,60).build());

            // LVL 3 HIGHEST
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.THREE, PotionManager.PotionDuration.HIGHEST),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(55,60).build());

            // ===== NIVEL 4 =====
            // LVL 4 LOW
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FOUR, PotionManager.PotionDuration.LOW),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(61,80).build());

            // LVL 4 MEDIUM
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FOUR, PotionManager.PotionDuration.MEDIUM),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(65,80).build());

            // LVL 4 HIGH
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FOUR, PotionManager.PotionDuration.HIGH),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(70,80).build());

            // LVL 4 HIGHEST
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FOUR, PotionManager.PotionDuration.HIGHEST),0.2)
                            .requiredBossDifficulty(BossDifficulty.HARD).levelRange(75,80).build());

            // ===== NIVEL 5 =====
            // LVL 5 LOW
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FIVE, PotionManager.PotionDuration.LOW),0.2)
                            .requiredBossDifficulty(BossDifficulty.EASY).levelRange(81,100).build());

            // LVL 5 MEDIUM
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FIVE, PotionManager.PotionDuration.MEDIUM),0.2)
                            .requiredBossDifficulty(BossDifficulty.NORMAL).levelRange(85,100).build());

            // LVL 5 HIGH
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FIVE, PotionManager.PotionDuration.HIGH),0.2)
                            .requiredBossDifficulty(BossDifficulty.HARD).levelRange(90,100).build());

            // LVL 5 HIGHEST
            conditionalDrops.add(
                    new ConditionalDropConfig
                            .Builder(Items.instance()
                            .getPotion(stat, PotionManager.PotionLevel.FIVE, PotionManager.PotionDuration.HIGHEST),0.2)
                            .requiredBossDifficulty(BossDifficulty.EXTREME).levelRange(95,100).build());
        }
    }

    private void addUpgraders() {
        // ===== FORJAS INESTABLES (type=1) - Más comunes en niveles bajos =====
        // Nivel 1 - Inestable (muy común en niveles bajos)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(1, 1), 0.08)
                .levelRange(1, 40).build());

        // Nivel 2 - Inestable (menos común pero aún en niveles bajos-medios)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(2, 1), 0.04)
                .levelRange(21, 60).build());

        // ===== FORJAS ACTIVAS (type=2) - Niveles medios + tier 1 bosses =====
        // Nivel 1 - Activa (niveles medios + EASY/NORMAL)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(1, 2), 0.06)
                .levelRange(31, 100).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        // Nivel 2 - Activa (niveles medios + EASY/NORMAL)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(2, 2), 0.04)
                .levelRange(41, 100).minimumBossDifficulty(BossDifficulty.HARD).build());

        // ===== FORJAS ESTABLES (type=3) - MÁS RARAS - Niveles altos + HARD/EXTREME =====
        // Nivel 1 - Estable (niveles medios-altos + HARD/EXTREME)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(1, 3), 0.03)
                .levelRange(75, 100).minimumBossDifficulty(BossDifficulty.HARD).build());

        // Nivel 2 - Estable (MUY RARO - solo niveles altos + HARD/EXTREME)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getUpgrader(2, 3), 0.02)
                .levelRange(91, 100).minimumBossDifficulty(BossDifficulty.EXTREME).build());
    }



    private void addXpMultipliers() {
        // ===== MULTIPLICADORES XP - Rareza progresiva según el valor =====

        // XP 100% - Más común (niveles bajos-medios)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getXpMultiplier(100), 0.1)
                .levelRange(1, 60).minimumBossDifficulty(BossDifficulty.EASY).build());

        // XP 200% - Menos común (niveles medios)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getXpMultiplier(200), 0.09)
                .levelRange(20, 80).minimumBossDifficulty(BossDifficulty.NORMAL).build());

        // XP 300% - Raro (niveles medios-altos)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getXpMultiplier(300), 0.08)
                .levelRange(40, 80).minimumBossDifficulty(BossDifficulty.HARD).build());

        // XP 400% - Muy raro (niveles altos)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getXpMultiplier(400), 0.07)
                .levelRange(60, 90).minimumBossDifficulty(BossDifficulty.HARD).build());

        // XP 500% - Extremadamente raro (solo niveles muy altos + dificultad máxima)
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getXpMultiplier(500), 0.06)
                .levelRange(80, 100).minimumBossDifficulty(BossDifficulty.EXTREME).build());
    }


    private  void addGemRemovers(){

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getGemremover(1),0.05 )
                .amount(1).levelRange(1,30).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getGemremover(2),0.1 )
                .amount(1).levelRange(31,60).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getGemremover(3),0.1 )
                .amount(1).levelRange(61,100).minimumBossDifficulty(BossDifficulty.NORMAL).build());

    }

    private void addOtherObjects(){
        //Identify scroll
        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getIdentifyScroll(), 0.2)
                .amount(1).requiresHostile().build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getBlessingBall(), 0.5)
                .minimumBossDifficulty(BossDifficulty.EASY).levelRange(1,50).amount(1,2).build());

        conditionalDrops.add(new ConditionalDropConfig.Builder(Items.instance().getSocketStone(), 0.5)
                .minimumBossDifficulty(BossDifficulty.EASY).amount(1,2).levelRange(1,50).build());

    }

}
