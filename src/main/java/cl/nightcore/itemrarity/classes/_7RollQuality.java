package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _7RollQuality implements RollQuality {

    private static final _7RollQuality INSTANCE = new _7RollQuality();

    // Constructor privado para evitar instanciación externa
    private _7RollQuality() {}

    // Metodo para obtener la instancia única
    public static _7RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 18; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 13; }
    @Override public int getBound() { return 22; }
}