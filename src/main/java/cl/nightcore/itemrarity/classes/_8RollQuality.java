package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _8RollQuality implements RollQuality {

    private static final _8RollQuality INSTANCE = new _8RollQuality();

    // Constructor privado para evitar instanciación externa
    private _8RollQuality() {}

    // Metodo para obtener la instancia única
    public static _8RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 18; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 14; }
    @Override public int getBound() { return 24; }
}