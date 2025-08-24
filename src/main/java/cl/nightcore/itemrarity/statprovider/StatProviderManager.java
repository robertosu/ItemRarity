package cl.nightcore.itemrarity.statprovider;

public class StatProviderManager {

    private final BootsModifierProvider bootsModifierProvider = new BootsModifierProvider();
    private final ChestplateModifierProvider chestplateModifierProvider = new ChestplateModifierProvider();
    private final HelmetModifierProvider helmetModifierProvider = new HelmetModifierProvider();
    private final LeggingsModifierProvider leggingsModifierProvider = new LeggingsModifierProvider();
    private final WeaponModifierProvider weaponModifierProvider = new WeaponModifierProvider();


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
