package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _3RollQuality implements RollQuality {
    private static final _3RollQuality INSTANCE = new _3RollQuality();

    // Constructor privado para evitar instanciación externa
    private _3RollQuality() {}

    // Metodo para obtener la instancia única
    public static _3RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 15; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 9; }
    @Override public int getBound() { return 14; }
}
