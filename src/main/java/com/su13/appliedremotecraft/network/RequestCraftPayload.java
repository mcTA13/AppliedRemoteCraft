package com.su13.appliedremotecraft.network;

import appeng.api.stacks.AEKey;
import com.su13.appliedremotecraft.AppliedRemoteCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: JEIで中クリックされた材料のクラフト要求 */
public record RequestCraftPayload(AEKey key) implements CustomPacketPayload {
    public static final Type<RequestCraftPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedRemoteCraft.MODID, "request_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCraftPayload> STREAM_CODEC =
            AEKey.STREAM_CODEC.map(RequestCraftPayload::new, RequestCraftPayload::key);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
