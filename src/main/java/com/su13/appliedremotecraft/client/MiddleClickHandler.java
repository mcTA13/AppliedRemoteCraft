package com.su13.appliedremotecraft.client;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.su13.appliedremotecraft.AppliedRemoteCraft;
import com.su13.appliedremotecraft.client.jei.ARCJeiPlugin;
import com.su13.appliedremotecraft.network.RequestCraftPayload;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import mezz.jei.api.constants.VanillaTypes;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/** JEI上の材料を中クリックしたらクラフト要求を送る。JEI(NORMAL)より先に処理するためHIGH。 */
@EventBusSubscriber(modid = AppliedRemoteCraft.MODID, value = Dist.CLIENT)
public final class MiddleClickHandler {
    private static boolean swallowRelease;

    private MiddleClickHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return;
        }
        IJeiRuntime runtime = ARCJeiPlugin.getRuntime();
        if (runtime == null) {
            return;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || !connection.hasChannel(RequestCraftPayload.TYPE)) {
            return;
        }

        AEKey key = findKeyUnderMouse(runtime, event);
        if (key == null) {
            return;
        }

        connection.send(new RequestCraftPayload(key));
        swallowRelease = true;
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (swallowRelease && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            swallowRelease = false;
            event.setCanceled(true);
        }
    }

    @Nullable
    private static AEKey findKeyUnderMouse(IJeiRuntime runtime, ScreenEvent event) {
        // レシピ画面（レシピ内スロット・触媒を含む）
        if (event.getScreen() instanceof IRecipesGui recipesGui) {
            Optional<ItemStack> item = recipesGui.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
            if (item.isPresent()) {
                return AEItemKey.of(item.get());
            }
            Optional<FluidStack> fluid = recipesGui.getIngredientUnderMouse(NeoForgeTypes.FLUID_STACK);
            if (fluid.isPresent()) {
                return toKey(fluid.get());
            }
        }

        // 材料一覧 → ブックマーク
        Optional<ITypedIngredient<?>> typed = runtime.getIngredientListOverlay().getIngredientUnderMouse();
        if (typed.isEmpty()) {
            typed = runtime.getBookmarkOverlay().getIngredientUnderMouse();
        }
        return typed.map(MiddleClickHandler::toKey).orElse(null);
    }

    @Nullable
    private static AEKey toKey(ITypedIngredient<?> typed) {
        Optional<ItemStack> item = typed.getIngredient(VanillaTypes.ITEM_STACK);
        if (item.isPresent()) {
            return AEItemKey.of(item.get());
        }
        return typed.getIngredient(NeoForgeTypes.FLUID_STACK).map(MiddleClickHandler::toKey).orElse(null);
    }

    @Nullable
    private static AEKey toKey(FluidStack fluid) {
        return fluid.isEmpty() ? null : AEFluidKey.of(fluid);
    }
}
