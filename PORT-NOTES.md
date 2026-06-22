# Port Notes: NeoForge to Fabric (1.21.1)

This document captures every adaptation made when porting BlockUI from NeoForge 1.21.1 to Fabric.

Base commit: `78dae57f044e47f0e9b31390dda86f861963d402` on `version/main`.
Target branch: `version/fabric`.

---

## 1. Event/Callback Mapping (NeoForge to Fabric)

| NeoForge Event | Fabric Replacement | Notes |
|---|---|---|
| `RegisterClientReloadListenersEvent` | `ReloadableResourceManager.registerReloadListener()` inside `ClientLifecycleEvents.CLIENT_STARTED` | Called from `BlockUIClient.onInitializeClient`, deferred to `CLIENT_STARTED` so the resource manager is ready. |
| `RegisterColorHandlersEvent.Block` | `ColorProviderRegistry.BLOCK.register()` | Called during client init. |
| `ModMismatchEvent` | Dropped | No Fabric equivalent. BlockUI stores no world data, so mismatch handling is unnecessary. |
| `ClientTickEvent.Pre` | `ClientTickEvents.START_CLIENT_TICK` | Replaces pre-tick subscriber. |
| `ClientTickEvent.Post` | `ClientTickEvents.END_CLIENT_TICK` | Replaces post-tick subscriber. |
| `MouseScrollingEvent` | `MouseHandlerMixin` (mixin into `MouseHandler.onScroll`) | Mixin cancels the call and forwards to `HookManager.onScroll` when no screen is active. See section 5. |
| `TagsUpdatedEvent` | `ContainerTagReloadListener` via `ReloadableResourceManager.registerReloadListener()` | Registered as a resource reload listener that triggers `ContainerHook` refresh after tags reload. |
| `RenderGuiLayerEvent.Pre` (crosshair suppression) | `GuiMixin` (mixin into `Gui.renderCrosshair`) | Mixin cancels crosshair rendering when a BOScreen is open or a scroll listener is active. See section 5. |

---

## 2. Networking Mapping (NeoForge to Fabric)

### Registration

| NeoForge | Fabric | Notes |
|---|---|---|
| `PayloadRegistrar.play(typeId)` | `PayloadTypeRegistry.playS2C().register(type, codec)` or `PayloadTypeRegistry.playC2S().register(type, codec)` | `PlayMessageType.register()` handles both codec and handler registration internally. The `register(Object)` overload is retained for source compatibility but ignores its argument. |
| `IPayloadContext` | `PlayMessageContext` (new interface) | Wraps `ClientPlayNetworking.Context` and `ServerPlayNetworking.Context`. Provides `flow()`, `player()`, `responseSender()`, and `enqueueWork(Runnable)`. |

### Sending

| NeoForge | Fabric | Notes |
|---|---|---|
| `PacketDistributor.sendToPlayer(player, payload)` | `ServerPlayNetworking.send(player, payload)` | Called from `IClientboundDistributor.sendToPlayer(ServerPlayer)`. |
| `PacketDistributor.sendToPlayersInDimension(level, payload)` | `PlayerLookup.world(serverLevel)` then `ServerPlayNetworking.send` per player | `IClientboundDistributor.sendToDimension(ServerLevel)`. |
| `PacketDistributor.sendToAllClients(payload)` | `PlayerLookup.all(server)` then send per player | `IClientboundDistributor.sendToAllClients()`. Server reference cached from `ServerLifecycleEvents.SERVER_STARTED`. |
| Client-to-server send | `ClientPlayNetworking.send(payload)` | `IServerboundDistributor.sendToServer()`. |

### Source-compatible API preserved

These public types keep the same signatures as the NeoForge version. Downstream mods (Structurize, Minecolonies) compile without changes:

- `AbstractPlayMessage` (bidirectional, public abstract class)
- `AbstractClientPlayMessage` (server-to-client, public abstract class)
- `AbstractServerPlayMessage` (client-to-server, public abstract class)
- `PlayMessageType<T>` (public record)
- `PlayMessageContext` (public interface)
- `IClientboundDistributor` (public interface with default methods for fan-out)
- `IServerboundDistributor` (public interface with `sendToServer()`)

---

## 3. Config Mapping (NeoForge to Fabric)

### Builder and value types

| NeoForge | Fabric | Notes |
|---|---|---|
| `ModConfigSpec.Builder` | `AbstractConfiguration.Builder` | Same method signatures: `push`, `pop`, `comment`, `translation`, `worldRestart`, `gameRestart`, `define`, `defineInRange`, `defineList`, `defineEnum`. |
| `ModConfigSpec.BooleanValue` | `AbstractConfiguration.BooleanValue` | Same `get()`, `set()`, `save()` interface. |
| `ModConfigSpec.IntValue` | `AbstractConfiguration.IntValue` | Same interface. |
| `ModConfigSpec.LongValue` | `AbstractConfiguration.LongValue` | Same interface. |
| `ModConfigSpec.DoubleValue` | `AbstractConfiguration.DoubleValue` | Same interface. |
| `ModConfigSpec.EnumValue` | `AbstractConfiguration.EnumValue` | Same interface. |
| `ModConfigSpec.ConfigValue<T>` | `AbstractConfiguration.ConfigValue<T>` | Same `get()`, `set()`, `save()`, `getDefault()` interface. |

### Backend

| NeoForge | Fabric | Notes |
|---|---|---|
| `ConfigTracker.INSTANCE.registerConfig(type, config)` | `ConfigBackend` (custom) | Reads and writes `config/blockui.toml`. Loaded during `Configurations` constructor. |
| `IConfigScreenFactory` | Cloth Config `ConfigBuilder` + ModMenu integration | `ClientConfigHelper` builds the config screen with Cloth Config entries. Registered via `ModContainer.registerConfigScreenFactory`. |
| `LogicalSidedProvider.WORKQUEUE.get()` | `Minecraft.getInstance().execute()` (client) / `server.execute()` (server) | Used in `PlayMessageContext` implementations and `ConfigWatcher` to schedule work on the main thread. |

### Source-compatible API preserved

- `AbstractConfiguration` (public abstract class with protected builder methods)
- `Configurations<CLIENT, SERVER, COMMON>` (public class)
- `ClientConfigHelper` (public class)
- All builder, value, and spec types nested in `AbstractConfiguration`

---

## 4. ModelData Emulation

The T1 spike confirmed that vanilla `BlockRenderDispatcher.renderSingleBlock` (which has no `ModelData` parameter) is sufficient for BlockUI's GUI icon rendering.

Key findings:

- `FakeLevel.modelDataManager` was not actively populated or consumed in the GUI icon rendering path.
- `BlockStateRenderingData` previously captured `blockEntity.getModelData()` and passed it to NeoForge's `renderSingleBlock` overload. Since vanilla has no such overload, the `ModelData` field was removed entirely.
- `FakeLevel.getModelData()` and `FakeLevel.getModelDataManager()` overrides were deleted.
- `FakeChunk.getModelData()` override was deleted.

Result: no mixins and no AW entries were needed for ModelData. The vanilla rendering path is used directly via `BOGuiGraphics`.

---

## 5. Mixin Inventory

Budget: 8 mixins maximum. Currently using 2.

| Mixin | Target | Purpose | Why a mixin instead of an AW? |
|---|---|---|---|
| `GuiMixin` | `net.minecraft.client.gui.Gui` | Suppress crosshair rendering when a `BOScreen` is open or `HookManager` has an active scroll listener. Injects at `renderCrosshair` HEAD, cancels if conditions are met. | Behavior interception. An AW can widen access but cannot inject into or cancel method execution. |
| `MouseHandlerMixin` | `net.minecraft.client.MouseHandler` | Intercept scroll events for `HookManager` when no screen is active. Injects at `onScroll` HEAD, cancels if `HookManager.onScroll` consumes the event. | Behavior interception. Same reason: AW cannot inject or cancel. |

Total: 2 of 8 budget used.

Config: `src/main/resources/blockui.mixins.json`
- `required: true`
- `package: com.ldtteam.blockui.mixin`
- `compatibilityLevel: JAVA_21`
- Both mixins are client-only (listed under `"client"`)

---

## 6. Access Widener Inventory

File: `src/main/resources/blockui.accessWidener`
Header: `accessWidener v2 named`

18 entries total, translated from 15 original Access Transformer entries in the deleted `src/main/resources/META-INF/accesstransformer.cfg`. Three AT entries required two AW lines each (one `accessible` plus one `mutable`) because the fields needed both read and write access.

| # | AW Entry | Original AT Entry | Purpose |
|---|---|---|---|
| 1 | `accessible field net/minecraft/world/entity/LivingEntity dead Z` | `public LivingEntity.dead` | Check if player is dead to close UI. |
| 2 | `accessible method net/minecraft/network/chat/Style <init> (...)V` | `public Style.<init>(...)` | Full constructor for custom chat styles. |
| 3 | `accessible method net/minecraft/client/gui/GuiGraphics <init> (...)V` | `public GuiGraphics.<init>(...)` | Custom GuiGraphics construction for BOGuiGraphics. |
| 4 | `accessible field net/minecraft/client/gui/GuiGraphics minecraft Lnet/minecraft/client/Minecraft;` | `public GuiGraphics.minecraft` | Access Minecraft instance from GuiGraphics. |
| 5 | `accessible method net/minecraft/server/packs/resources/FallbackResourceManager convertToMetadata (...)Lnet/minecraft/server/packs/resources/IoSupplier;` | `public FallbackResourceManager.convertToMetadata(...)` | Sprite loading for out-of-jar textures. |
| 6 | `accessible field com/mojang/blaze3d/platform/NativeImage pixels J` | `public NativeImage.pixels` | Cursor texture pixel manipulation. |
| 7 | `mutable field com/mojang/blaze3d/platform/NativeImage pixels J` | (split from #6) | Write access to pixel data for cursor rendering. |
| 8 | `accessible field com/mojang/blaze3d/vertex/PoseStack poseStack Ljava/util/Deque;` | `public PoseStack.poseStack` | Direct stack access for custom pose operations. |
| 9 | `mutable field com/mojang/blaze3d/vertex/PoseStack poseStack Ljava/util/Deque;` | (split from #8) | Write access for pose stack manipulation. |
| 10 | `accessible field net/minecraft/client/resources/model/ModelBakery modelResources Ljava/util/Map;` | `public ModelBakery.modelResources` | Block state rendering for GUI icons. |
| 11 | `accessible field net/minecraft/client/resources/model/ModelBakery topLevelModels Ljava/util/Map;` | `public ModelBakery.topLevelModels` | Block state rendering for GUI icons. |
| 12 | `accessible field net/minecraft/client/gui/GuiSpriteManager METADATA_SECTIONS Ljava/util/Set;` | `public GuiSpriteManager.METADATA_SECTIONS` | Texture atlas sprite management. |
| 13 | `accessible field net/minecraft/client/resources/TextureAtlasHolder textureAtlas Lnet/minecraft/client/renderer/texture/TextureAtlas;` | `public TextureAtlasHolder.textureAtlas` | Texture atlas access for custom sprites. |
| 14 | `accessible field net/minecraft/client/gui/components/AbstractButton SPRITES Lnet/minecraft/client/gui/components/WidgetSprites;` | `public AbstractButton.SPRITES` | Vanilla button texture access. |
| 15 | `extendable class net/minecraft/resources/ResourceLocation` | `public-f ResourceLocation` | Subclassing ResourceLocation for `OutOfJarResourceLocation`. |
| 16 | `accessible method net/minecraft/resources/ResourceLocation <init> (Ljava/lang/String;Ljava/lang/String;)V` | `protected ResourceLocation.<init>(String,String)` | Two-argument constructor for ResourceLocation subclasses. |
| 17 | `accessible field net/minecraft/client/renderer/texture/HttpTexture file Ljava/io/File;` | `public HttpTexture.file` | Out-of-jar texture file access. |
| 18 | `mutable field net/minecraft/client/renderer/texture/HttpTexture file Ljava/io/File;` | (split from #17) | Write access for texture file redirection. |

---

## 7. Known Deviations

These are intentional behavior changes from the NeoForge version, made because Fabric lacks equivalent APIs or has different lifecycle patterns:

1. **`BOWindow.openAsLayer()`**: Uses `Minecraft.setScreen()` with `previousScreen` tracking instead of `pushGuiLayer`/`popGuiLayer`. Fabric has no screen layer system. The previous screen is saved and restored manually.

2. **`BOScreen.FABRIC_GUI_FAR_PLANE = 21000.0F`**: Replaces `ClientHooks.getGuiFarPlane()` (NeoForge-only). Set to 21000.0F to match vanilla MC 1.21.1's orthographic far plane (`setOrtho(0, w, h, 0, 1000.0F, 21000.0F`). With this value, the model-view translate becomes `10000 - 21000 = -11000`, matching vanilla's `-11000.0F` exactly. The original port used 1000.0F which created a degenerate projection (near == far) and made the GUI invisible.

3. **`BlockStateRenderingData`**: The `ModelData` field is removed. Evaluation changed from lazy (via `Lazy<>`) to eager. The T1 spike proved this is safe for all current GUI rendering paths.

4. **`FakeLevel`**: `getModelData()` and `getModelDataManager()` overrides are deleted. These were not consumed in the GUI icon path.

5. **`BOGuiGraphics`**: Uses vanilla `renderSingleBlock` (no `ModelData` parameter) and vanilla camera transforms. The NeoForge overload with `ModelData` does not exist on Fabric.

6. **`AbstractTextElement`**: `NeoForgeRenderTypes.enableTextTextureLinearFiltering` toggles are removed. Fabric has no equivalent render-type toggle.

7. **`ItemIconWithProperties`**: Uses `ClampedItemPropertyFunction` instead of `ItemPropertyFunction`. This is a 1.21.1 API change in vanilla, not Fabric-specific.

8. **`ColouredVertexConsumer.misc()`**: Removed. This was a NeoForge-only API for custom vertex consumption.

9. **`OutOfJarResourceLocation.compareNamespaced()`**: The namespace-first comparison is inlined. Vanilla `ResourceLocation` lacks the `compareNamespaced` method that NeoForge adds.

10. **Reload listener registration via `ResourceManagerHelper`**: `Loader.INSTANCE` (XML cache), the container tag refresh listener, and `AtlasManager.INSTANCE` (texture atlas) are all registered via `ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener()` in `BlockUIClient.onInitializeClient()`. This ensures they participate in the initial client resource reload. `ClientLifecycleEvents.CLIENT_STARTED` fires AFTER the initial reload, so registering there leaves caches empty. Color providers and tick event handlers still register in `CLIENT_STARTED` as they don't need the initial reload.

11. **`AtlasManager` implements `IdentifiableResourceReloadListener`**: In vanilla Fabric, `AtlasManager` must implement `IdentifiableResourceReloadListener` and be registered via `ResourceManagerHelper` to participate in the initial resource reload. The `CustomGuiSpriteManager` is created lazily inside `reload()` where `Minecraft.getInstance().getTextureManager()` is available. Without this, the custom sprite atlas is empty (0x0), causing `Invalid texture size: 0x0` crashes when dumping or rendering atlas contents.

---

## 9. Vanilla-Clean File Modifications

The port plan's original constraint was "widget/view/XML/Pane code stays byte-identical (except @Environment annotations)." In practice, several vanilla-clean files reference NeoForge-added APIs that do not exist in vanilla MC 1.21.1. These files MUST be modified for the code to compile.

### Changes by category:

#### A. NeoForge API → Vanilla API Replacements (required for compilation)

| File | NeoForge API Removed | Vanilla Replacement | Justification |
|---|---|---|---|
| `views/BOWindow.java` | `Minecraft.pushGuiLayer()`, `Minecraft.popGuiLayer()` | Manual `previousScreen` tracking via `BOScreen.setPreviousScreen()`/`getPreviousScreen()` | `pushGuiLayer`/`popGuiLayer` are NeoForge additions to `Minecraft`. Vanilla MC 1.21.1 has no screen layer system. |
| `controls/ItemIcon.java` | `CreativeModeTabRegistry.getSortedCreativeModeTabs()` | `CreativeModeTabs.allTabs()` | `CreativeModeTabRegistry` is a NeoForge class. `CreativeModeTabs.allTabs()` is the vanilla equivalent. |
| `controls/ItemIconWithProperties.java` | `ItemPropertyFunction` type | `ClampedItemPropertyFunction` type | Vanilla MC 1.21.1 changed `ItemProperties.register()` to accept `ClampedItemPropertyFunction` instead of `ItemPropertyFunction`. This is a vanilla API change, not Fabric-specific. |
| `controls/AbstractTextElement.java` | `NeoForgeRenderTypes.enableTextTextureLinearFiltering` | Computed but unused (TODO comment) | `NeoForgeRenderTypes` is a NeoForge-only class. The linear filtering value is still computed but not applied (Fabric has no equivalent runtime toggle). |
| `BOScreen.java` | `ClientHooks.getGuiFarPlane()`, `NeoForgeRenderTypes.enableTextTextureLinearFiltering` | `FABRIC_GUI_FAR_PLANE` constant (21000.0F), filtering toggles removed | Both are NeoForge-only APIs. The far plane value matches vanilla MC 1.21.1 exactly. |

#### B. @Environment / FMLEnvironment Swaps (expected per plan)

| File | Change |
|---|---|
| `views/BOWindow.java` | `@OnlyIn(Dist.CLIENT)` → `@Environment(EnvType.CLIENT)` |
| `controls/ButtonImage.java` | `FMLEnvironment.production` → `EnvUtil.isProduction()` |
| `controls/CheckBox.java` | `FMLEnvironment.production` → `EnvUtil.isProduction()` |
| `controls/Image.java` | `FMLEnvironment.production` → `EnvUtil.isProduction()` (2 locations) |

#### C. Minor Compile Fixes

| File | Change | Justification |
|---|---|---|
| `PaneParams.java` | Added `import org.jetbrains.annotations.Nullable;` | Needed for compilation after NeoForge imports removed. |
| `util/color/ColouredVertexConsumer.java` | Removed `NeoForgeRenderTypes` import and `misc()` method override | NeoForge-only API. |
| `util/texture/CursorTexture.java` | Removed unused `import com.ldtteam.blockui.Pane;` | Unused import that caused compile error when Pane wasn't on classpath. |
| `util/texture/OutOfJarTexture.java` | Minor import fix | NeoForge import removal. |
| `util/resloc/OutOfJarResourceLocation.java` | Inlined `compareNamespaced()` comparison | Vanilla `ResourceLocation` lacks this NeoForge-added method. |

### New Public API Additions

The port adds two new public methods to `BOScreen`:
- `public void setPreviousScreen(Screen previousScreen)` 
- `public Screen getPreviousScreen()`

These are required because vanilla MC 1.21.1 has no screen layer system. The original `pushGuiLayer`/`popGuiLayer` calls in `BOWindow` are replaced with manual screen tracking. This is the minimum API surface needed to preserve the original behavior (opening/closing windows with screen restoration).

### Summary

All changes to vanilla-clean files fall into three categories:
1. **NeoForge API → Vanilla API replacements** — required for compilation (NeoForge classes don't exist)
2. **@Environment/FMLEnvironment swaps** — expected per port plan
3. **Minor compile fixes** — import removals and inline replacements

No behavioral changes were made beyond what's required by the NeoForge→Fabric API differences.

## 10. Cherry-Pick Guide

When upstream `version/main` receives fixes, you can apply them to `version/fabric`:

### Basic workflow

```bash
git fetch upstream
git log --oneline upstream/version/main    # find the commits you need
git checkout version/fabric
git cherry-pick <commit-sha>
```

### Conflict resolution

If a cherry-pick conflicts in a ported file, resolve manually and keep the Fabric changes. The Fabric branch's version of these files intentionally differs from upstream.

### Files likely to conflict

These files contain Fabric-specific adaptations. Cherry-picks touching them will need manual resolution:

- `com/ldtteam/blockui/mod/` (entrypoints, lifecycle subscribers)
- `com/ldtteam/common/network/` (Fabric Networking API)
- `com/ldtteam/common/config/` (Cloth Config backend)
- `com/ldtteam/common/fakelevel/` (ModelData removal)
- `com/ldtteam/blockui/mod/item/BlockStateRenderingData.java` (ModelData field removed)
- `src/main/resources/fabric.mod.json` (Fabric metadata)
- `src/main/resources/blockui.accessWidener` (AW, not AT)
- `build.gradle` (Fabric Loom, not NeoForge)

### Files unlikely to conflict

These files are vanilla-clean and should cherry-pick without issues:

- `com/ldtteam/blockui/Pane.java` (completely unmodified)
- `com/ldtteam/blockui/controls/TextField.java` (completely unmodified)
- `com/ldtteam/blockui/controls/` — most files only have @Environment swaps, but some have NeoForge API replacements (see Section 9)
- `com/ldtteam/blockui/views/` — most files are unmodified, but `BOWindow.java` has NeoForge API replacements (see Section 9)
- `com/ldtteam/blockui/util/` (color, utility classes)
- `com/ldtteam/blockui/` package (pure UI logic)
- `src/main/resources/assets/` (textures, XML, lang files)
