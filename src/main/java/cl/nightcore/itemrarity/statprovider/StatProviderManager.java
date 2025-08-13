package cl.nightcore.itemrarity.statprovider;

public class StatProviderManager {

    private BootsModifierProvider bootsModifierProvider = new BootsModifierProvider();
    private ChestplateModifierProvider chestplateModifierProvider = new ChestplateModifierProvider();
    private HelmetModifierProvider helmetModifierProvider = new HelmetModifierProvider();
    private LeggingsModifierProvider leggingsModifierProvider = new LeggingsModifierProvider();

    private WeaponModifierProvider weaponModifierProvider = new WeaponModifierProvider();


    public BootsModifierProvider boots(){
        return bootsModifierProvider;
    }

    public ChestplateModifierProvider chestplate(){
        return chestplateModifierProvider;
    }

    public HelmetModifierProvider helmet(){
        return helmetModifierProvider;
    }

    public  LeggingsModifierProvider leggings(){
        return leggingsModifierProvider;
    }

    public WeaponModifierProvider weapon(){
        return weaponModifierProvider;
    }

}
