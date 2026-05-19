package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

public class SaveSlotCard {

    public static final float WIDTH  = 800f;
    public static final float HEIGHT = 100f;

    private static final float PAD             = 24f;
    private static final int   BORDER_NORMAL   = 2;
    private static final int   BORDER_SELECTED = 4;

    private final float       x, y;
    private final String      slotLabel;
    private final GlyphLayout slotLayout;

    private boolean selected;
    private boolean corrupted;

    public SaveSlotCard(int slotIndex, float x, float y) {
        this.x          = x;
        this.y          = y;
        this.slotLabel  = "SLOT " + (slotIndex + 1);
        this.slotLayout = new GlyphLayout(Fonts.sansLarge, slotLabel);
    }

    public void render(ShapeRenderer shapes, SpriteBatch batch) {
        Color borderColor;
        int   bw;

        if (corrupted) {
            borderColor = Palette.ERROR;
            bw          = BORDER_NORMAL;
        } else if (selected) {
            borderColor = Palette.PRIMARY;
            bw          = BORDER_SELECTED;
        } else {
            borderColor = Palette.ON_SURFACE;
            bw          = BORDER_NORMAL;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(borderColor);
        shapes.rect(x, y, WIDTH, HEIGHT);
        shapes.setColor(Palette.SURFACE);
        shapes.rect(x + bw, y + bw, WIDTH - 2 * bw, HEIGHT - 2 * bw);
        shapes.end();

        batch.begin();
        if (corrupted) {
            // Title in upper portion, warning below
            Fonts.sansLarge.setColor(Palette.ON_SURFACE);
            Fonts.sansLarge.draw(batch, slotLabel, x + PAD, y + HEIGHT - 18f);

            Fonts.sansMedium.setColor(Palette.ERROR);
            Fonts.sansMedium.draw(batch, "! ARCHIVO CORRUPTO – no se puede cargar", x + PAD, y + 38f);
        } else {
            // Vertically centered title
            float titleY = y + (HEIGHT + slotLayout.height) / 2f;
            Fonts.sansLarge.setColor(selected ? Palette.PRIMARY : Palette.ON_SURFACE);
            Fonts.sansLarge.draw(batch, slotLabel, x + PAD, titleY);
        }
        batch.end();
    }

    public void setSelected(boolean selected)   { this.selected  = selected; }
    public void setCorrupted(boolean corrupted) { this.corrupted = corrupted; }
    public boolean isSelected()                 { return selected; }
    public boolean isCorrupted()                { return corrupted; }
}
