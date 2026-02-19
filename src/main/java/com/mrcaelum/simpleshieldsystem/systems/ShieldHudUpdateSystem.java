package com.mrcaelum.simpleshieldsystem.systems;

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
import com.mrcaelum.simpleshieldsystem.ui.ShieldHud;

import javax.annotation.Nonnull;
import java.util.Map;

public final class ShieldHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private static final float SPEED = 8f;
    private static final float EPSILON = 0.001f;

    private final ComponentType<EntityStore, EntityStatMap> STAT_MAP;
    private final ComponentType<EntityStore, Player> PLAYER;
    private final ComponentType<EntityStore, PlayerRef> PLAYER_REF;

    private final int shieldStatIndex;
    private final Map<PlayerRef, ShieldHud> hudMap;

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
        float actual = hud.getShieldValue();

        if (actual == normalized) return;

        float delta = normalized - actual;
        float displayValue = Math.abs(delta) < EPSILON ? normalized
                : actual + (delta * Math.min(1f, SPEED * dt));

        UICommandBuilder builder = new UICommandBuilder();
        hud.setShieldValue(displayValue, builder);
        hud.update(false, builder);
    }
}

