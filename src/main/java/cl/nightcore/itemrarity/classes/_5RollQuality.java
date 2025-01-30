package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _5RollQuality implements RollQuality {

    private static final _5RollQuality INSTANCE = new _5RollQuality();

    // Constructor privado para evitar instanciación externa
    private _5RollQuality() {}

    // Metodo para obtener la instancia única
    public static _5RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 16; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 11; }
    @Override public int getBound() { return 18; }
}