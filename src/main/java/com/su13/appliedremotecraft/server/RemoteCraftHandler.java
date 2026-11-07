package com.su13.appliedremotecraft.server;

import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftAmountMenu;
import com.su13.appliedremotecraft.ModItems;
import com.su13.appliedremotecraft.network.RequestCraftPayload;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public final class RemoteCraftHandler {
    private RemoteCraftHandler() {
    }

    public static void handle(RequestCraftPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                requestCraft(player, payload.key());
            }
        });
    }

    private static void requestCraft(ServerPlayer player, AEKey key) {
        ItemMenuHostLocator locator = findTerminal(player);
        if (locator == null) {
            fail(player, Component.translatable("message.appliedremotecraft.no_terminal"));
            return;
        }

        // コンストラクタ内でアクセスポイント・電力・リンク状態が更新される
        WirelessTerminalMenuHost<?> host = locator.locate(player, WirelessTerminalMenuHost.class);
        if (host == null) {
            fail(player, Component.translatable("message.appliedremotecraft.no_terminal"));
            return;
        }
        if (!host.getLinkStatus().connected()) {
            Component reason = host.getLinkStatus().statusDescription();
            fail(player, reason != null ? reason : Component.translatable("message.appliedremotecraft.not_connected"));
            return;
        }

        IGridNode node = host.getActionableNode();
        if (node == null || !node.getGrid().getCraftingService().isCraftable(key)) {
            fail(player, Component.translatable("message.appliedremotecraft.not_craftable", key.getDisplayName()));
            return;
        }

        CraftAmountMenu.open(player, locator, key, key.getAmountPerUnit());
    }

    /** Curios → インベントリの順に、カード入りのユニバーサルターミナルを探す */
    @Nullable
    private static ItemMenuHostLocator findTerminal(ServerPlayer player) {
        IItemHandler curios = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (curios != null) {
            for (int i = 0; i < curios.getSlots(); i++) {
                if (isValidTerminal(curios.getStackInSlot(i))) {
                    return MenuLocators.forCurioSlot(i);
                }
            }
        }

        var inventory = player.getInventory();
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

    private static void fail(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
