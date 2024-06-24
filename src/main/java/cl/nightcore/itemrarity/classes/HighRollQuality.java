package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class HighRollQuality implements RollQuality {
    public HighRollQuality(){}
    @Override
    public int getMean() {return 14;}
    @Override
    public double getSd() {return 6.67;}
    @Override
    public int getMinStatValue() {return 9;}
    @Override
    public int getBound() {return 12;}
}
