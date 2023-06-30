package me.refracdevelopment.simpletags.utilities.menu;

import me.refracdevelopment.simpletags.config.Menus;
import me.refracdevelopment.simpletags.utilities.Utilities;
import me.refracdevelopment.simpletags.utilities.chat.Color;

/**
 * A class extending the functionality of the regular Menu, but making it Paginated
 */
public abstract class PaginatedMenu extends Menu {

    protected int page = 0;
    protected int maxItemsPerPage = 28;
    protected int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public void addMenuBorder() {
        inventory.setItem(48, makeItem(Utilities.getMaterial(Menus.TAGS_ITEMS.getString("left.material")).parseMaterial(), Color.translate(Menus.TAGS_ITEMS.getString("left.name"))));

        inventory.setItem(49, makeItem(Utilities.getMaterial(Menus.TAGS_ITEMS.getString("close.material")).parseMaterial(), Color.translate(Menus.TAGS_ITEMS.getString("close.name"))));

        inventory.setItem(50, makeItem(Utilities.getMaterial(Menus.TAGS_ITEMS.getString("right.material")).parseMaterial(), Color.translate(Menus.TAGS_ITEMS.getString("right.name"))));

        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        inventory.setItem(17, super.FILLER_GLASS);
        inventory.setItem(18, super.FILLER_GLASS);
        inventory.setItem(26, super.FILLER_GLASS);
        inventory.setItem(27, super.FILLER_GLASS);
        inventory.setItem(35, super.FILLER_GLASS);
        inventory.setItem(36, super.FILLER_GLASS);

        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }
}