package cl.nightcore.itemrarity.abstracted;

import java.util.List;

import cl.nightcore.itemrarity.item.Gem;
import org.bukkit.inventory.ItemStack;

public interface Socketable {
    int getMaxSockets();
    int getCurrentSockets();
    List<Gem> getInstalledGems();
    boolean addSocket();
    boolean hasEmptySockets();
    boolean installGem(Gem gem);
    Gem removeGem(int socketIndex);
    void updateSocketDisplay();
}