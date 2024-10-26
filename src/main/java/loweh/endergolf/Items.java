package loweh.endergolf;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * A class to hold items created by the Ender Golf mod.
 */
public class Items {
    /**
     * Should be called in the ModInitializer's onInitialize() function. Registers the items created in this class
     * to their corresponding itemGroup.
     */
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(Items.SCORECARD));
    }

    /**
     * Helper function to register an item.
     * @param item Item object of type T
     * @param id Name of the item
     * @return Class extending Item
     * @param <T> Class extending Item
     */
    public static <T extends Item> T register(T item, String id) {
        Identifier itemID = Identifier.of("endergolf", id);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static final Item SCORECARD = register(new Scorecard(new Item.Settings()), "scorecard");
}
