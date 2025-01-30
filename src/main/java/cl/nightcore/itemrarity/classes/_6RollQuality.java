package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _6RollQuality implements RollQuality {
    private static final _6RollQuality INSTANCE = new _6RollQuality();

    // Constructor privado para evitar instanciación externa
    private _6RollQuality() {}

    // Metodo para obtener la instancia única
    public static _6RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 17; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 12; }
    @Override public int getBound() { return 20; }
}