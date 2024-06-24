package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class MediumRollQuality implements RollQuality {
    public MediumRollQuality(){}
    @Override
    public int getMean() {return 12;}
    @Override
    public double getSd() {return 6.67;}
    @Override
    public int getMinStatValue() {return 7;}
    @Override
    public int getBound() {return 12;}
}
