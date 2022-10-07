package abyss.plugin.api;

import abyss.map.Region;
import abyss.map.WorldObject;
import abyss.plugin.api.actions.attributes.DefaultPluginAttributeSerializer;
import abyss.plugin.api.actions.attributes.PluginAttributes;
import abyss.plugin.api.extensions.Extension;
import abyss.plugin.api.extensions.ExtensionContainer;
import abyss.plugin.api.widgets.InventoryWidgetExtension;
import abyss.plugin.api.world.WorldTile;
import com.abyss.definitions.ObjectType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * All plugins must extend this base type.
 */
public abstract class Plugin implements ExtensionContainer<Extension> {

    public final PluginAttributes persistentAttributes = new PluginAttributes(new HashMap<>(), DefaultPluginAttributeSerializer.INSTANCE);
    public final PluginAttributes attributes = new PluginAttributes(new HashMap<>(), DefaultPluginAttributeSerializer.INSTANCE);

    public final Map<Integer, VarbitRequest> requestedVarbits = new HashMap<>();
    public final Map<Integer, VarpRequest> requestedVarps = new HashMap<>();

    private final Map<Class<?>, Extension> pluginExtensions = new HashMap<>();

    /**
     * A random instance that is seeded with information about the running account.
     */
    private final Random accountSpecificRandom = new Random(Abyss.getStaticRngSeed(3));

    /**
     * A random instance that is secure.
     */
    private final Random secureRandom = new SecureRandom();

    /**
     * Called when the plugin is loaded into the client.
     *
     * @return If the plugin will run or not.
     */
    public boolean onLoaded(PluginContext pluginContext) {
        return true;
    }

    /**
     * Called when this plugin is enabled in the plugin list.
     */
    public void onEnabled() {
    }

    /**
     * Called when this plugin is disabled in the plugin list.
     */
    public void onDisabled() {

    }

    /**
     * Called when the client is ticking, and it's time for us
     * to loop again. The client will wait for the amount of milliseconds
     * you return before calling this function again.
     *
     * @return The amount of time to wait before invoking this function again.
     */
    public int onLoop() {
        return 60000;
    }

    /**
     * Called when the server sends the end of tick packet
     * @param self - The Local Player
     * @param tickCount - The tick count since login (resets to 0 on logout)
     * @return - the number of ticks to wait
     */

    public int onServerTick(Player self, long tickCount) {
        return 0;
    }

    /**
     * Called when the plugin's window is being painted.
     */
    public void onPaint() {

    }

    /**
     * Called when the client's 3d overlay is being painted.
     */
    public void onPaintOverlay() {

    }

    /**
     * Called when the runescape client asks for the value of a varbit
     *
     * @param varbitId - The Varbit ID
     * @param conVarId - The ConVarID that the varbit is stored in
     * @param value    - The Value of the requested Varbit
     */

    public void onVarbitRequest(int varbitId, int conVarId, int value) {
        if (!requestedVarbits.containsKey(varbitId)) {
            requestedVarbits.put(varbitId, new VarbitRequest(varbitId, conVarId, value));
        } else {
            VarbitRequest req = requestedVarbits.get(varbitId);
            req.setValue(value);
        }
    }

    /**
     * Called when the RuneScape client asks for the value of a varp (ConVar)
     * @param varpId The varp id
     * @param value The varp value
     */

    public void onVarpRequest(int varpId, int value) {
        if(!requestedVarps.containsKey(varpId)) {
            requestedVarps.put(varpId, new VarpRequest(varpId, value));
        } else {
            VarpRequest req = requestedVarps.get(varpId);
            req.setValue(value);
        }
    }

    /**
     * Called when the visibility of a widget changes.
     */
    public void onWidgetVisibilityChanged(int id, boolean visible) {

    }

    /**
     * Called when the local player is changed. This is useful for initializing plugin data
     * about the local player.
     */
    public void onLocalPlayerChanged(Player self) {

    }

    /**
     * Called when RuneScape gets a spawn object packet
     * @param objectId The object id
     * @param x The x coordinate where the object is being spawned
     * @param y The y coordinate where the object is being spawned
     * @param type The type of object (wall, groun decor, etc)
     */

    public void onSceneObjectSpawned(int objectId, int x, int y, int type) {

    }

    private void onSceneObjectAdded(int objectId, int x, int y, int type) {
        Player self = Players.self();
        if(self == null) {
            return;
        }
        Vector3i pos = self.getGlobalPosition();
        WorldTile tile = new WorldTile(x, y, 0);
        Region region = Region.get(tile.getRegionId());
        ObjectType otype = ObjectType.forId(type);
        if(otype == null) {
            return;
        }
        WorldObject wo = new WorldObject(x, y, pos.getZ(), objectId, otype);
        region.spawnObject(wo, pos.getZ(), tile.getXInRegion(), tile.getYInRegion());

        onSceneObjectSpawned(objectId, x, y, type);
    }

    /**
     * Called when RuneScape sends destroy scene object packet.
     * @param objectId The object id to be destroyed
     * @param x The x coordinate of the scene object to be destroyed
     * @param y The y coordinate of the scene object to be destroyed
     * @param type The type of the scene object to be destroyed
     */

    public void onSceneObjectDestroyed(int objectId, int x, int y, int type) {

    }

    private void onSceneObjectRemoved(int x, int y, int type) {
        Player self = Players.self();
        if(self == null) {
            return;
        }
        Vector3i pos = self.getGlobalPosition();
        WorldTile tile = new WorldTile(x, y, 0);
        Region region = Region.get(tile.getRegionId());
        ObjectType otype = ObjectType.forId(type);
        if(otype == null) {
            return;
        }
        WorldObject wo = region.objects[pos.getZ()][tile.getXInRegion()][tile.getYInRegion()][otype.slot];
        region.destroyObject(wo, pos.getZ(), tile.getXInRegion(), tile.getYInRegion());

        onSceneObjectDestroyed(wo.getId(), wo.getX(), wo.getY(), wo.getType().id);
    }

    /**
     * Called when an item in the inventory is changed.
     */
    private void inventoryItemChanged(WidgetItem prev, WidgetItem next) {
        if (!Inventory.INVENTORY.hasExtension(InventoryWidgetExtension.class)) {
            return;
        }
        InventoryWidgetExtension ext = (InventoryWidgetExtension) Inventory.INVENTORY.getExt(InventoryWidgetExtension.class);
        ItemContainer inventory = ItemContainers.byId(ext.getContainerId());
        prev.setContainer(inventory);
        next.setContainer(inventory);
        onInventoryItemChanged(prev, next);
    }

    public void onInventoryItemChanged(WidgetItem prev, WidgetItem next) {

    }

    /**
     * Called to determine if a break should be interrupted currently.
     */
    public boolean interruptBreak() {
        return false;
    }

    public final void setAttribute(String key, int value) {
        persistentAttributes.put(key, Integer.toString(value));
    }

    public final void setAttribute(String key, boolean value) {
        persistentAttributes.put(key, Boolean.toString(value));
    }

    public final void setAttribute(String key, double value) {
        persistentAttributes.put(key, Double.toString(value));
    }

    public final void setAttribute(String key, float value) {
        persistentAttributes.put(key, Float.toString(value));
    }

    public final void setAttribute(String key, String value) {
        persistentAttributes.put(key, value);
    }

    public final int getInt(String key) {
        try {
            return persistentAttributes.containsKey(key) ? Integer.parseInt(persistentAttributes.getOrDefault(key, "0")) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public final boolean getBoolean(String key) {
        try {
            return persistentAttributes.containsKey(key) && Boolean.parseBoolean(persistentAttributes.get(key));
        } catch (Exception e) {
            return false;
        }
    }

    public final double getDouble(String key) {
        try {
            return persistentAttributes.containsKey(key) ? Double.parseDouble(persistentAttributes.getOrDefault(key, "0.0")) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public final double getFloat(String key) {
        try {
            return persistentAttributes.containsKey(key) ? Float.parseFloat(persistentAttributes.getOrDefault(key, "0.0")) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public final String getString(String key) {
        return persistentAttributes.getOrDefault(key, "");
    }

    public final void save(PluginContext context) {
        pluginExtensions.forEach((key, value) -> {
            value.save(persistentAttributes);
        });
        ByteArrayOutputStream out = persistentAttributes.serialize(persistentAttributes);
        context.setPersistentData(out.toByteArray());
    }

    public final void load(PluginContext context) {
        persistentAttributes.deserialize(persistentAttributes, new ByteArrayInputStream(context.getPersistentData()));
        pluginExtensions.forEach((key, value) -> {
            value.load(persistentAttributes);
        });
    }

    /**
     * @return A random instance that is seeded with information about the running account.
     */
    public Random getAccountSpecificRandom() {
        return accountSpecificRandom;
    }

    /**
     * @return A random instance that is secure.
     */
    public Random getSecureRandom() {
        return secureRandom;
    }

    @NotNull
    @Override
    public Extension getExt(@NotNull Class<?> clazz) {
        return pluginExtensions.get(clazz);
    }

    @Override
    public boolean hasExtension(@NotNull Class<?> clazz) {
        return pluginExtensions.containsKey(clazz);
    }

    @Override
    public void setExtension(@NotNull Extension extension) {
        pluginExtensions.put(extension.getClass(), extension);
    }

    @NotNull
    @Override
    public List<Extension> getExtensions() {
        return pluginExtensions.values().stream().toList();
    }
}
