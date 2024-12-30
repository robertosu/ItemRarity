package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class MediumRollQuality implements RollQuality {
    public MediumRollQuality(){}
    @Override
    public int getMean() {return 14;}
    @Override
    public double getSd() {return 6.67;}
    @Override
    public int getMinStatValue() {return 5;}
    @Override
    public int getBound() {return 15;}
}
