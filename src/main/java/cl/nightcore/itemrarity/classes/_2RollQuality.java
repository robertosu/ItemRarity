package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _2RollQuality implements RollQuality {
    private static final _2RollQuality INSTANCE = new _2RollQuality();

    // Constructor privado para evitar instanciación externa
    private _2RollQuality() {}

    // Metodo para obtener la instancia única
    public static _2RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 14; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 8; }
    @Override public int getBound() { return 12; }
}