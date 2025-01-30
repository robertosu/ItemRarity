package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _9RollQuality implements RollQuality {

    private static final _9RollQuality INSTANCE = new _9RollQuality();

    // Constructor privado para evitar instanciación externa
    private _9RollQuality() {}

    // Metodo para obtener la instancia única
    public static _9RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 19; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 15; }
    @Override public int getBound() { return 26; }
}