package cl.nightcore.itemrarity.util;

public enum ItemType {
    // Armaduras
    HELMET(Category.ARMOR),
    CHESTPLATE(Category.ARMOR),
    LEGGINGS(Category.ARMOR),
    BOOTS(Category.ARMOR),

    // Armas principales
    SWORD(Category.WEAPON),
    AXE(Category.WEAPON),
    MACE(Category.WEAPON),
    TRIDENT(Category.WEAPON),

    // Armas a distancia
    BOW(Category.RANGED_WEAPON),
    CROSSBOW(Category.RANGED_WEAPON),

    // Desconocido
    UNKNOWN(Category.UNKNOWN);

    private final Category category;

    ItemType(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    // Métodos de conveniencia
    public boolean isArmor() {
        return category == Category.ARMOR;
    }

    public boolean isWeapon() {
        return category == Category.WEAPON || category == Category.RANGED_WEAPON;
    }

    public boolean isMainWeapon() {
        return category == Category.WEAPON;
    }

    public boolean isRangedWeapon() {
        return category == Category.RANGED_WEAPON;
    }

    public enum Category {
        ARMOR,
        WEAPON,
        RANGED_WEAPON,
        UNKNOWN
    }
}