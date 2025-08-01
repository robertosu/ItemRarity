package cl.nightcore.itemrarity.config;

import cl.nightcore.itemrarity.item.*;
import com.nexomc.nexo.mechanics.breakable.N;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;

import javax.naming.Name;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;

public class ItemConfig {

    public static final String ROLLCOUNT_KEY = "magicobject_roll_count";
    public static final String ITEM_LEVEL_KEY = "item_level";
    public static final String SCROLLED_IDENTIFIER_KEY = "is_identify_scrolled";
    public static final String MAX_BONUSES_KEY = "maxbonuses";

    public static final NamespacedKey LEVEL_KEY_NS;
    public static final NamespacedKey ROLLCOUNT_KEY_NS;
    public static final NamespacedKey SCROLLED_IDENTIFIER_KEY_NS;
    public static final NamespacedKey MAX_BONUSES_KEY_NS;
    public static final NamespacedKey GEM_REMOVER_KEY_NS;
    public static final NamespacedKey ITEM_UPGRADER_KEY_NS;
    public static final NamespacedKey GEM_UPGRADER_KEY_NS;
    public static final NamespacedKey GEM_STAT_KEY_NS;
    public static final NamespacedKey SOCKET_GEM_KEY_NS;

    public static final NamespacedKey IDENTIFY_SCROLL_KEY_NS;
    public static final NamespacedKey REDEMPTION_OBJECT_KEY_NS;
    public static final NamespacedKey MAGIC_OBJECT_KEY_NS;
    public static final NamespacedKey BLESSING_OBJECT_KEY_NS;
    public static final NamespacedKey BLESSING_BALL_KEY_NS;

    public static final TextColor COMMON_COLOR = TextColor.color(0xDEDEDE);
    public static final TextColor UNCOMMON_COLOR = TextColor.color(0x2CAF34);
    public static final TextColor RARE_COLOR = TextColor.color(0x0333AF);
    public static final TextColor EPIC_COLOR = TextColor.color(0x812EDD);
    public static final TextColor LEGENDARY_COLOR =TextColor.color(0xFFBF00);
    public static final TextColor GODLIKE_COLOR = TextColor.color(0xFF0003);

    public static final Component PLUGIN_PREFIX = Component.text("[Pergamino]: ").color(NamedTextColor.GOLD);
    public static final Component REROLL_PREFIX = Component.text("[Objeto Mágico]: ").color(MagicObject.getPrimaryColor());
    public static final Component REDEMPTION_PREFIX = Component.text("[Redención]: ").color(RedemptionObject.getPrimaryColor());
    public static final Component BLESSING_PREFIX = Component.text("[Bendición]: ").color(BlessingObject.getPrimaryColor());
    public static final Component GEMSTONE_PREFIX = Component.text("[Gemas]: ").color(GemObject.getPrimaryColor());
    public static final Component BLESSING_BALL_PREFIX = Component.text("[Bola Bendición]: ").color(BlessingBall.getPrimaryColor());
    public static final Component ITEM_UPGRADER_PREFIX = Component.text("[Forja]: ");
    public static final Component GEM_REMOVER_PREFIX = Component.text("[Removedor]: ").color(GemRemover.getPrimaryColor());
    public static final Component GEM_UPGRADER_PREFIX = Component.text("[Forja de gemas]: ").color(GemRemover.getPrimaryColor());
    public static final Component XP_MULTIPLIER_PREFIX = Component.text("[Multiplicador XP]: ").color(BlessingObject.getPrimaryColor());
    public static final Component STATPOTION_PREFIX = Component.text("[StatPotion]: ").color(NamedTextColor.LIGHT_PURPLE);

    public static final String BLESSING_OBJECT_KEY = "blessing_object";
    public static final String GEM_REMOVER_KEY = "gem_remover";
    public static final String IDENTIFY_SCROLL_KEY = "identify_scroll";

    public static final String MAGIC_OBJECT_KEY = "magic_object";
    public static final String REDEMPTION_OBJECT_KEY = "redemption_object";
    public static final String BLESSING_BALL_KEY = "blessing_ball";
    public static final String ITEM_UPGRADER_KEY = "item_upgrader";
    public static final String GEM_STAT_KEY = "gem_stat";
    public static final String GEM_LEVEL_KEY = "gem_level";
    public static final String GEM_UPGRADER_KEY = "gem_upgrader";
    public static final String SOCKET_GEM_KEY = "socket_gem";




    static {
        LEVEL_KEY_NS = new NamespacedKey(PLUGIN, ITEM_LEVEL_KEY);
        ROLLCOUNT_KEY_NS = new NamespacedKey(PLUGIN, ROLLCOUNT_KEY);
        SCROLLED_IDENTIFIER_KEY_NS = new NamespacedKey(PLUGIN, SCROLLED_IDENTIFIER_KEY);
        MAX_BONUSES_KEY_NS = new NamespacedKey(PLUGIN, MAX_BONUSES_KEY);
        GEM_REMOVER_KEY_NS = new NamespacedKey(PLUGIN, GEM_REMOVER_KEY);
        ITEM_UPGRADER_KEY_NS = new NamespacedKey(PLUGIN, ITEM_UPGRADER_KEY);
        GEM_STAT_KEY_NS = new NamespacedKey(PLUGIN, GEM_STAT_KEY);
        GEM_UPGRADER_KEY_NS = new NamespacedKey(PLUGIN,GEM_UPGRADER_KEY);
        SOCKET_GEM_KEY_NS = new NamespacedKey(PLUGIN,SOCKET_GEM_KEY);
        IDENTIFY_SCROLL_KEY_NS = new NamespacedKey(PLUGIN,IDENTIFY_SCROLL_KEY);
        REDEMPTION_OBJECT_KEY_NS = new NamespacedKey(PLUGIN,REDEMPTION_OBJECT_KEY);
        MAGIC_OBJECT_KEY_NS = new NamespacedKey(PLUGIN,MAGIC_OBJECT_KEY);
        BLESSING_OBJECT_KEY_NS = new NamespacedKey(PLUGIN,BLESSING_OBJECT_KEY);
        BLESSING_BALL_KEY_NS = new NamespacedKey(PLUGIN,BLESSING_BALL_KEY);

    }
}
