package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

/**
 * Inventory submenu panel: 2-column grid of items with stat labels.
 * Renders at arbitrary (x, y, w, h) — caller provides combat-box bounds.
 */
public class ItemListWidget {

    private static final int   COLS     = 2;
    private static final int   BORDER   = 2;
    private static final float PAD      = 12f;
    private static final float ROW_H    = 34f;
    private static final float FOOTER_H = 28f;

    // Dummy data: {name, statLabel}
    private static final String[][] ITEMS = {
        {"Pocion",       "20 HP" },
        {"Manzana",      "+3 ATK"},
        {"Daga",         "+2 ATK"},
        {"Escudo Viejo", "+1 DEF"},
    };

    private final GlyphLayout[] statLayouts = new GlyphLayout[ITEMS.length];
    private int selectedIndex = 0;

    public ItemListWidget() {
        for (int i = 0; i < ITEMS.length; i++) {
            statLayouts[i] = new GlyphLayout(Fonts.monoTiny, ITEMS[i][1]);
        }
    }

    /** dx/dy: -1, 0, or +1. Wraps within the 2-col grid. */
    public void navigate(int dx, int dy) {
        int col    = selectedIndex % COLS;
        int row    = selectedIndex / COLS;
        int maxRow = (ITEMS.length - 1) / COLS;
        col = Math.floorMod(col + dx, COLS);
        row = Math.floorMod(row + dy, maxRow + 1);
        int next = row * COLS + col;
        selectedIndex = Math.min(next, ITEMS.length - 1);
    }

    public int    getSelectedIndex() { return selectedIndex; }
    public String getSelectedName()  { return ITEMS[selectedIndex][0]; }
    public void   reset()            { selectedIndex = 0; }

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

        // Footer separator
        float sepY = innerY + FOOTER_H;
        shapes.setColor(Palette.SECONDARY);
        shapes.rect(innerX + PAD, sepY, innerW - 2 * PAD, 1f);
        shapes.end();

        float colW        = (innerW - 2 * PAD) / COLS;
        float contentTopY = y + h - BORDER - PAD; // first row baseline (y-up)

        batch.begin();

        // Items grid
        for (int i = 0; i < ITEMS.length; i++) {
            int   col  = i % COLS;
            int   row  = i / COLS;
            float itemX = innerX + PAD + col * colW;
            float itemY = contentTopY - row * ROW_H;
            boolean sel = (i == selectedIndex);

            // Name with cursor
            Fonts.sansSmall.setColor(sel ? Palette.PRIMARY : Palette.ON_SURFACE);
            Fonts.sansSmall.draw(batch, (sel ? "> " : "  ") + ITEMS[i][0], itemX, itemY);

            // Stat label, right-aligned within its column
            Fonts.monoTiny.setColor(sel ? Palette.PRIMARY : Palette.SECONDARY);
            float statX = itemX + colW - statLayouts[i].width - PAD;
            Fonts.monoTiny.draw(batch, ITEMS[i][1], statX, itemY);
        }

        // Footer hint
        Fonts.monoTiny.setColor(Palette.SECONDARY);
        Fonts.monoTiny.draw(batch, "Z: USAR   X: VOLVER",
            innerX + PAD,
            innerY + FOOTER_H * 0.7f + Fonts.monoTiny.getCapHeight());

        batch.end();
    }
}
