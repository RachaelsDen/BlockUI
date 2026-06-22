package com.ldtteam.blockui.mixin;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.hooks.HookManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin
{
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void blockui$hideCrosshair(final GuiGraphics guiGraphics, final DeltaTracker deltaTracker, final CallbackInfo ci)
    {
        if (Minecraft.getInstance().screen instanceof BOScreen || HookManager.getScrollListener() != null)
        {
            ci.cancel();
        }
    }
}
