package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.ItemRarity;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RolledWeapon extends IdentifiedWeapon{

    private static final int MAX_LEVEL = 30;

    public RolledWeapon(ItemStack item) {
        super(item);
        setNBTTag();
    }
    private void setNBTTag() {
        NBTItem nbtItem = new NBTItem(this);
        nbtItem.setInteger(ROLL_IDENTIFIER_KEY, 1);
        nbtItem.mergeNBT(this);
    }

   public void incrementLevel(Player player) {
       NBTItem nbtItem = new NBTItem(this);
       int lvl = nbtItem.getInteger(LEVEL_KEY);

       if (lvl < MAX_LEVEL) {

           lvl += 1;
           player.sendMessage(ItemRarity.getRerollPrefix() + "El item subió su nivel de magia " + ChatColor.BLUE + lvl +ChatColor.WHITE+ " / " + ChatColor.BLUE + MAX_LEVEL);
           if (lvl ==10){
               player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 2");
           }else if(lvl==20){
               player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 3");
           }else if(lvl==30){
               player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 4");
           }
           nbtItem = new NBTItem(this);
           nbtItem.setInteger(LEVEL_KEY, lvl);
           nbtItem.mergeNBT(this);

       }
   }
}
