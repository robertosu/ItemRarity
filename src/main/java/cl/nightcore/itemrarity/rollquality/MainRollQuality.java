package cl.nightcore.itemrarity.rollquality;

public class MainRollQuality implements RollQuality {

    private static final MainRollQuality INSTANCE = new MainRollQuality();

    private MainRollQuality() {}

    // Metodo para obtener la instancia única
    public static MainRollQuality getInstance() {
        return INSTANCE;
    }

    @Override public int getMean() { return 19; }
    @Override public double getSd() { return 6.67; }
}