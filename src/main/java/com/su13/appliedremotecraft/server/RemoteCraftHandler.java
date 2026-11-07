package com.su13.appliedremotecraft.server;

import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEKey;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.me.crafting.CraftAmountMenu;
import com.su13.appliedremotecraft.TerminalFinder;
import com.su13.appliedremotecraft.network.RequestCraftPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
        // カード入りのターミナルが無ければ何もしない
        ItemMenuHostLocator locator = TerminalFinder.findTerminal(player);
        if (locator == null) {
            return;
        }

        // コンストラクタ内でアクセスポイント・電力・リンク状態が更新される
        WirelessTerminalMenuHost<?> host = locator.locate(player, WirelessTerminalMenuHost.class);
        if (host == null) {
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

    private static void fail(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
