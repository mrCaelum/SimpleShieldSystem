package com.mrcaelum.simpleshieldsystem;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrcaelum.simpleshieldsystem.systems.FlatShieldDamageSystem;
import com.mrcaelum.simpleshieldsystem.systems.ShieldHudUpdateSystem;
import com.mrcaelum.simpleshieldsystem.ui.ShieldHud;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * SimpleShieldSystem - Simple Shield System for Hytale
 *
 * @author mrCaelum
 * @version 0.0.3
 */
public class SimpleShieldSystemPlugin extends JavaPlugin {
    private static final Map<PlayerRef, ShieldHud> playerRefShieldHudMap = new HashMap<>();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static SimpleShieldSystemPlugin instance;
    private static ShieldHudUpdateSystem shieldHudUpdateSystem = null;

    public SimpleShieldSystemPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static SimpleShieldSystemPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("Setting up...");
        EventRegistry eventBus = getEventRegistry();
        try {
            eventBus.registerGlobal(LoadedAssetsEvent.class, this::onAssetsLoaded);
            eventBus.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
            eventBus.registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
            LOGGER.at(Level.INFO).log("Registered player event listeners");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("Failed to register player event listeners");
        }
        LOGGER.at(Level.INFO).log("Setup complete!");
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("Started!");
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("Shutting down...");
        instance = null;
    }

    /**
     * Handle player ready event.
     * @param event The player ready event
     */
    @SuppressWarnings("rawtypes")
    private void onAssetsLoaded(LoadedAssetsEvent event) {
        if (event.getAssetClass() != EntityStatType.class) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, EntityStatType> loadedAssets = (Map<String, EntityStatType>) event.getLoadedAssets();

        if (!loadedAssets.containsKey("Shield")) {
            return;
        }

        int shieldStatIndex = EntityStatType.getAssetMap().getIndex("Shield");

        getEntityStoreRegistry().registerSystem(
                new FlatShieldDamageSystem(shieldStatIndex)
        );
        if (shieldHudUpdateSystem == null) {
            shieldHudUpdateSystem = new ShieldHudUpdateSystem(
                    shieldStatIndex,
                    playerRefShieldHudMap
            );
            getEntityStoreRegistry().registerSystem(shieldHudUpdateSystem);
        }
        LOGGER.at(Level.INFO).log("Registered \"Shield\" stat with index " + shieldStatIndex);
    }

    /**
     * Handle player ready event.
     * @param event The player ready event
     */
    private void onPlayerReady(PlayerReadyEvent event) {
        Ref<EntityStore> playerRefStore = event.getPlayerRef();
        Store<EntityStore> store = playerRefStore.getStore();
        PlayerRef playerRef = store.getComponent(playerRefStore, PlayerRef.getComponentType());

        if (playerRef == null) {
            return;
        }

        Ref<EntityStore> ref = playerRef.getReference();

        if (ref == null || !ref.isValid()) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            return;
        }

        ShieldHud shieldHud = new ShieldHud(playerRef);
        PluginBase plugin = PluginManager.get().getPlugin(PluginIdentifier.fromString("Buuz135:MultipleHUD"));
        if (plugin != null) {
            LOGGER.at(Level.INFO).log("MultipleHUD plugin found.");
            MultipleHUD.getInstance().setCustomHud(player, playerRef, "ShieldHUD", shieldHud);
        }
        else {
            LOGGER.at(Level.INFO).log("MultipleHUD plugin not found. Create base custom hud.");
            player.getHudManager().setCustomHud(playerRef, shieldHud);
        }
        shieldHud.show();
        playerRefShieldHudMap.put(playerRef, shieldHud);
    }

    /**
     * Handle player disconnect event.
     * @param event The player disconnect event
     */
    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();

        playerRefShieldHudMap.remove(playerRef);
    }
}