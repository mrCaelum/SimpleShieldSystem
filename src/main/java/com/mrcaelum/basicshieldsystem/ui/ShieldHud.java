package com.mrcaelum.simpleshieldsystem.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShieldHud extends CustomUIHud {

    private float currentShieldValue;

    public ShieldHud(@NonNullDecl PlayerRef playerRef) {
        super(playerRef);
        currentShieldValue = 0f;
    }

    @Override
    public void build(UICommandBuilder builder) {
        builder.append("Hud/Shield/Shield.ui");
        setShieldValue(0.0f, builder);
    }

    public float getShieldValue() {
        return currentShieldValue;
    }

    public void setShieldValue(float shieldValue, UICommandBuilder builder) {
        builder.set("#ShieldBar.Visible", shieldValue > 0);
        builder.set("#ProgressBarShield.Value", shieldValue);
        currentShieldValue = shieldValue;
    }
}
