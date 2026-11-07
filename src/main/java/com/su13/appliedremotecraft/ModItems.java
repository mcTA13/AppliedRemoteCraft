package com.su13.appliedremotecraft;

import appeng.api.upgrades.Upgrades;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedRemoteCraft.MODID);

    public static final DeferredItem<Item> REMOTE_CRAFT_CARD = ITEMS.registerItem("remote_craft_card",
            props -> Upgrades.createUpgradeCardItem(props.stacksTo(1)));

    private ModItems() {
    }
}
