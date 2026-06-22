package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.AtlasManager;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.hooks.HookRegistries;
import com.ldtteam.blockui.mod.container.ContainerHook;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class ClientEventSubscriber
{
    private static boolean registered;

    private ClientEventSubscriber()
    {
        // no instances
    }

    public static void register()
    {
        if (registered)
        {
            return;
        }

        ClientTickEvents.START_CLIENT_TICK.register(ClientEventSubscriber::onClientTickStart);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventSubscriber::onClientTickEnd);

        final ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        resourceManager.registerReloadListener(new ContainerTagReloadListener());
        ContainerHook.init();

        registered = true;
    }

    private static void onClientTickStart(final Minecraft minecraft)
    {
        if (minecraft.player == null || !Screen.hasAltDown() || !Screen.hasControlDown() || !Screen.hasShiftDown())
        {
            return;
        }

        if (!InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_X))
        {
            return;
        }

        final BOWindow window = new BOWindow();
        int id = 0;

        final Button dumpAtlases = createTestGuiButton(id++, "Dump mod atlases to run folder", null);
        dumpAtlases.setHandler(b -> {
            final Path dumpingFolder = Path.of("atlas_dump").toAbsolutePath().normalize();
            minecraft.player.sendSystemMessage(Component.literal("Dumping atlases into: " + dumpingFolder));
            AtlasManager.INSTANCE.dumpAtlases(dumpingFolder);
        });
        window.addChild(dumpAtlases);

        window.addChild(createTestGuiButton(id++, "General All-in-one", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test.xml"), parent -> {
            parent.findPaneOfTypeByID("missing_out_of_jar", Image.class)
                .setImage(OutOfJarResourceLocation.ofMinecraftFolder(BlockUI.MOD_ID, "missing_out_of_jar.png"), false);
            parent.findPaneOfTypeByID("working_out_of_jar", Image.class)
                .setImage(OutOfJarResourceLocation.of(BlockUI.MOD_ID, Path.of("../../src/test/resources/button.png")), false);
            OutOfJarResourceLocation.ofMinecraftSkin(minecraft, minecraft.getGameProfile(), null)
                .thenAccept(resLoc -> parent.findPaneOfTypeByID("player_skin", Image.class).setImage(resLoc, false));
            OutOfJarResourceLocation.ofMinecraftSkin(minecraft, minecraft.getGameProfile(), PlayerSkin::capeTexture)
                .thenAccept(resLoc -> {
                    if (resLoc != null)
                    {
                        parent.findPaneOfTypeByID("player_cape", Image.class).setImage(resLoc, false);
                    }
                });
            OutOfJarResourceLocation.ofMinecraftSkin(minecraft, minecraft.getGameProfile(), PlayerSkin::elytraTexture)
                .thenAccept(resLoc -> {
                    if (resLoc != null)
                    {
                        parent.findPaneOfTypeByID("player_elytra", Image.class).setImage(resLoc, false);
                    }
                });
        }));
        window.addChild(createTestGuiButton(id++, "Tooltip Positioning", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test2.xml")));
        window.addChild(createTestGuiButton(id++, "ItemIcon To BlockState", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test3.xml"), BlockStateTestGui::setup));
        window.addChild(createTestGuiButton(id++, "Scrolling Lists", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test4.xml"), ScrollingListsGui::setup));

        final Text builderTest = new Text();
        builderTest.setSize(ButtonImage.DEFAULT_BUTTON_WIDTH * 2 + 20, ButtonImage.DEFAULT_BUTTON_HEIGHT);
        builderTest.setPosition(0, ((id + 1) / 2) * (builderTest.getHeight() + 10));
        PaneBuilders.textBuilder()
            .append(Component.literal(BlockUI.MOD_ID))
            .append(Component.literal(" - "))
            .append(Component.literal(FabricLoader.getInstance().getModContainer(BlockUI.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown")))
            .paragraphBreak()
            .colorName("red")
            .underlined()
            .append(Component.translatable("blockui.tooltip.item_additional_info",
                Component.translatable("key.keyboard.left.control")
                    .append(" + ")
                    .append(Component.translatable("key.keyboard.left.shift"))
                    .append(" + ")
                    .append(Component.translatable("key.keyboard.left.alt"))
                    .setStyle(Style.EMPTY.withItalic(true))))
            .applyToPane(builderTest);
        window.addChild(builderTest);

        window.open();
    }

    private static void onClientTickEnd(final Minecraft minecraft)
    {
        if (minecraft.level != null)
        {
            minecraft.getProfiler().push("hook_manager_tick");
            HookRegistries.tick(minecraft.level.getGameTime());
            minecraft.getProfiler().pop();
        }
    }

    @SafeVarargs
    private static Button createTestGuiButton(final int order,
        final String name,
        final ResourceLocation testGuiResLoc,
        final Consumer<BOWindow>... setups)
    {
        final Button button = new ButtonImage(true);
        button.setPosition((order % 2) * (button.getWidth() + 20), (order / 2) * (button.getHeight() + 10));
        button.setText(Component.literal(name));
        button.setHandler(b -> {
            new BOWindow(testGuiResLoc)
            {
                @Override
                public void onOpened()
                {
                    super.onOpened();
                    for (final Consumer<BOWindow> setup : setups)
                    {
                        setup.accept(this);
                    }
                }
            }.openAsLayer();
        });
        return button;
    }

    private static final class ContainerTagReloadListener extends SimplePreparableReloadListener<Unit>
    {
        @Override
        protected Unit prepare(final ResourceManager resourceManager, final ProfilerFiller profiler)
        {
            return Unit.INSTANCE;
        }

        @Override
        protected void apply(final Unit prepared, final ResourceManager resourceManager, final ProfilerFiller profiler)
        {
            ContainerHook.init();
        }
    }
}
