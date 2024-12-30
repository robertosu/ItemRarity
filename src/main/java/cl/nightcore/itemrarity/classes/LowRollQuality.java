package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class LowRollQuality implements RollQuality {
    public LowRollQuality(){}
    @Override
    public int getMean() {return 12;}
    @Override
    public double getSd() {return 6.67;}
    @Override
    public int getMinStatValue() {return 3;}
    @Override
    public int getBound() {return 15;}
}
