package com.su13.appliedremotecraft;

import appeng.api.upgrades.IUpgradeableItem;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** カード入りのユニバーサルターミナルを探す（クライアント・サーバー共通） */
public final class TerminalFinder {
    private TerminalFinder() {
    }

    /** Curios → インベントリの順に探す */
    @Nullable
    public static ItemMenuHostLocator findTerminal(Player player) {
        IItemHandler curios = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (curios != null) {
            for (int i = 0; i < curios.getSlots(); i++) {
                if (isValidTerminal(curios.getStackInSlot(i))) {
                    return MenuLocators.forCurioSlot(i);
                }
            }
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (isValidTerminal(inventory.getItem(i))) {
                return MenuLocators.forInventorySlot(i);
            }
        }
        return null;
    }

    private static boolean isValidTerminal(ItemStack stack) {
        return !stack.isEmpty()
                && AE2wtlibAPI.isUniversalTerminal(stack)
                && stack.getItem() instanceof IUpgradeableItem upgradeable
                && upgradeable.getUpgrades(stack).isInstalled(ModItems.REMOTE_CRAFT_CARD.get());
    }
}
