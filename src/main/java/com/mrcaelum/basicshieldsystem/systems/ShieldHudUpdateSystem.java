package com.mrcaelum.basicshieldsystem.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.mrcaelum.basicshieldsystem.ui.ShieldHud;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class ShieldHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, EntityStatMap> STAT_MAP;
    private final ComponentType<EntityStore, Player> PLAYER;
    private final ComponentType<EntityStore, PlayerRef> PLAYER_REF;

    private final int shieldStatIndex;
    private final Map<PlayerRef, ShieldHud> hudMap;
    private final Map<PlayerRef, Float> lastShieldValues = new HashMap<>();

    public ShieldHudUpdateSystem(
            int shieldStatIndex,
            Map<PlayerRef, ShieldHud> hudMap
    ) {
        this.STAT_MAP = EntityStatMap.getComponentType();
        this.PLAYER = Player.getComponentType();
        this.PLAYER_REF = PlayerRef.getComponentType();
        this.shieldStatIndex = shieldStatIndex;
        this.hudMap = hudMap;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                PLAYER,
                PLAYER_REF,
                STAT_MAP
        );
    }

    @Override
    public void tick(
            float dt,
            int index,
            ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        PlayerRef playerRef = chunk.getComponent(index, PLAYER_REF);
        EntityStatMap statMap = chunk.getComponent(index, STAT_MAP);

        if (playerRef == null || statMap == null) return;

        ShieldHud hud = hudMap.get(playerRef);
        if (hud == null) return;

        EntityStatValue shield = statMap.get(shieldStatIndex);
        if (shield == null) return;

        float current = shield.get();
        float max = shield.getMax();
        float normalized = max <= 0 ? 0f : current / max;

        Float last = lastShieldValues.get(playerRef);
        if (last != null && Float.compare(last, normalized) == 0) {
            return;
        }

        lastShieldValues.put(playerRef, normalized);

        UICommandBuilder builder = new UICommandBuilder();
        hud.setShieldValue(normalized, builder);
        hud.update(false, builder);
    }

    public void removePlayerData(PlayerRef playerRef) {
        lastShieldValues.remove(playerRef);
    }
}

