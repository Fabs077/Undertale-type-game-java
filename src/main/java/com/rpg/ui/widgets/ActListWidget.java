package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

/**
 * ACT submenu panel: boss-name header bar + 2-column action list.
 * Renders at arbitrary (x, y, w, h) — caller provides combat-box bounds.
 */
public class ActListWidget {

    private static final int   COLS      = 2;
    private static final int   BORDER    = 2;
    private static final float PAD       = 12f;
    private static final float HEADER_H  = 24f;
    private static final float ROW_H     = 34f;
    private static final float FOOTER_H  = 28f;

    // Dummy act options
    private static final String[] ACTS = { "HABLAR", "INSULTAR", "ELOGIAR", "BROMEAR" };

    private int    selectedIndex = 0;
    private String bossName      = "BOSS";

    public void setBossName(String name) { this.bossName = name; }
    public void reset()                  { selectedIndex = 0; }

    /** dx/dy: -1, 0, or +1. Wraps within the 2-col grid. */
    public void navigate(int dx, int dy) {
        int col    = selectedIndex % COLS;
        int row    = selectedIndex / COLS;
        int maxRow = (ACTS.length - 1) / COLS;
        col = Math.floorMod(col + dx, COLS);
        row = Math.floorMod(row + dy, maxRow + 1);
        int next = row * COLS + col;
        selectedIndex = Math.min(next, ACTS.length - 1);
    }

    public int    getSelectedIndex() { return selectedIndex; }
    public String getSelectedAct()   { return ACTS[selectedIndex]; }

    public void render(SpriteBatch batch, ShapeRenderer shapes,
                       float x, float y, float w, float h) {
        float innerX = x + BORDER;
        float innerY = y + BORDER;
        float innerW = w - 2 * BORDER;
        float innerH = h - 2 * BORDER;

        // Panel background + 2px white border
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Palette.ON_SURFACE);
        shapes.rect(x, y, w, h);
        shapes.setColor(Palette.BG);
        shapes.rect(innerX, innerY, innerW, innerH);

        // Header bar: white background strip at the top of the inner area
        float headerY = y + h - BORDER - HEADER_H;
        shapes.setColor(Palette.ON_SURFACE);
        shapes.rect(innerX, headerY, innerW, HEADER_H);

        // Footer separator
        float sepY = innerY + FOOTER_H;
        shapes.setColor(Palette.SECONDARY);
        shapes.rect(innerX + PAD, sepY, innerW - 2 * PAD, 1f);
        shapes.end();

        float colW        = (innerW - 2 * PAD) / COLS;
        // Content starts below the header
        float contentTopY = headerY - PAD;

        batch.begin();

        // Header label "* BOSSNAME" in black on white bar
        Fonts.sansSmall.setColor(Palette.BG);
        float headerTextY = headerY + HEADER_H * 0.5f + Fonts.sansSmall.getCapHeight() * 0.5f;
        Fonts.sansSmall.draw(batch, "* " + bossName, innerX + PAD, headerTextY);

        // Acts grid
        for (int i = 0; i < ACTS.length; i++) {
            int   col  = i % COLS;
            int   row  = i / COLS;
            float actX = innerX + PAD + col * colW;
            float actY = contentTopY - row * ROW_H;
            boolean sel = (i == selectedIndex);

            Fonts.sansSmall.setColor(sel ? Palette.PRIMARY : Palette.ON_SURFACE);
            Fonts.sansSmall.draw(batch, (sel ? "> " : "  ") + ACTS[i], actX, actY);
        }

        // Footer hint
        Fonts.monoTiny.setColor(Palette.SECONDARY);
        Fonts.monoTiny.draw(batch, "Z: ACTUAR   X: VOLVER",
            innerX + PAD,
            innerY + FOOTER_H * 0.7f + Fonts.monoTiny.getCapHeight());

        batch.end();
    }
}
