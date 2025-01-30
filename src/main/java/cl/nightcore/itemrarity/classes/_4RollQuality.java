package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class _4RollQuality implements RollQuality {

    private static final _4RollQuality INSTANCE = new _4RollQuality();

    // Constructor privado para evitar instanciación externa
    private _4RollQuality() {}

    // Metodo para obtener la instancia única
    public static _4RollQuality getInstance() {
        return INSTANCE;
    }
    @Override public int getMean() { return 16; }
    @Override public double getSd() { return 6.67; }
    @Override public int getMinStatValue() { return 10; }
    @Override public int getBound() { return 16; }
}