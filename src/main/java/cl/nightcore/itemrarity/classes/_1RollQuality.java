package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _1RollQuality implements RollQuality {

    private static final _1RollQuality INSTANCE = new _1RollQuality();

    // Constructor privado para evitar instanciación externa
    private _1RollQuality() {}

    // Metodo para obtener la instancia única
    public static _1RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 14; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 7; }
    @Override public int getBound() { return 10; }
}