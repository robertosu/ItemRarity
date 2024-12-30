package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;

public class GodRollQuality implements RollQuality {
    public GodRollQuality(){}
    @Override
    public int getMean() {return 18;}
    @Override
    public double getSd() {return 6.67;}
    @Override
    public int getMinStatValue() {return 9;}
    @Override
    public int getBound() {return 15;}
}
