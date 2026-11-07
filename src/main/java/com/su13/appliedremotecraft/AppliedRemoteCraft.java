package com.su13.appliedremotecraft;

import appeng.api.upgrades.Upgrades;
import com.su13.appliedremotecraft.network.RequestCraftPayload;
import com.su13.appliedremotecraft.server.RemoteCraftHandler;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(AppliedRemoteCraft.MODID)
public class AppliedRemoteCraft {
    public static final String MODID = "appliedremotecraft";

    public AppliedRemoteCraft(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(AppliedRemoteCraft::commonSetup);
        modEventBus.addListener(AppliedRemoteCraft::registerPayloads);
        modEventBus.addListener(AppliedRemoteCraft::addCreative);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> Upgrades.add(ModItems.REMOTE_CRAFT_CARD.get(), AE2wtlibAPI.getWUT(), 1));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RequestCraftPayload.TYPE, RequestCraftPayload.STREAM_CODEC, RemoteCraftHandler::handle);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.REMOTE_CRAFT_CARD.get());
        }
    }
}
