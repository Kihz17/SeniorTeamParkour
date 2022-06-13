package com.kihz.mechanics;

import com.kihz.Constants;
import com.kihz.Core;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.MetadataUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class Restrictions extends GameMechanic {
    private static final String ITEM_WHITELIST = "itemOwner";

    /**
     * Whitelist an item drop so only a certain player can pick it up. Expires after a little bit of time.
     * @param item
     * @param owner
     */
    public static void whitelistItemDrop(Item item, Player owner) {
        if (owner == null)
            return;
        MetadataUtils.setMetadata(item, ITEM_WHITELIST, owner.getName());
        Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> MetadataUtils.removeMetadata(item, ITEM_WHITELIST), Constants.TPM);
    }
}
