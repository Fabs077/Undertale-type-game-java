package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.engine.items.Armor;
import com.rpg.engine.items.Consumable;
import com.rpg.engine.items.Item;
import com.rpg.engine.items.Weapon;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

import java.util.ArrayList;

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

    private ArrayList<Item>    items       = new ArrayList<>();
    private GlyphLayout[] statLayouts = new GlyphLayout[0];
    private int           selectedIndex = 0;

    /** Replaces the displayed list with the player's current inventory. */
    public void setItems(ArrayList<Item> newItems) {
        items = new ArrayList<>(newItems);
        statLayouts = new GlyphLayout[items.size()];
        for (int i = 0; i < items.size(); i++) {
            statLayouts[i] = new GlyphLayout(Fonts.monoTiny, statLabel(items.get(i)));
        }
        selectedIndex = 0;
    }

    private static String statLabel(Item item) {
        if (item instanceof Consumable c) return "+" + c.getHealAmount() + " HP";
        if (item instanceof Weapon w)    return "+" + w.getStatBonus()   + " ATK";
        if (item instanceof Armor a)     return "+" + a.getStatBonus()   + " DEF";
        return "";
    }

    /** dx/dy: -1, 0, or +1. Wraps within the 2-col grid. */
    public void navigate(int dx, int dy) {
        if (items.isEmpty()) return;
        int col    = selectedIndex % COLS;
        int row    = selectedIndex / COLS;
        int maxRow = (items.size() - 1) / COLS;
        col = Math.floorMod(col + dx, COLS);
        row = Math.floorMod(row + dy, maxRow + 1);
        int next = row * COLS + col;
        selectedIndex = Math.min(next, items.size() - 1);
    }

    public int  getSelectedIndex() { return selectedIndex; }
    public void reset()            { selectedIndex = 0; }

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
        float contentTopY = y + h - BORDER - PAD;

        batch.begin();

        if (items.isEmpty()) {
            Fonts.sansSmall.setColor(Palette.SECONDARY);
            Fonts.sansSmall.draw(batch, "Inventario vacío", innerX + PAD, contentTopY);
        } else {
            for (int i = 0; i < items.size(); i++) {
                int   col   = i % COLS;
                int   row   = i / COLS;
                float itemX = innerX + PAD + col * colW;
                float itemY = contentTopY - row * ROW_H;
                boolean sel = (i == selectedIndex);

                // Name with cursor
                Fonts.sansSmall.setColor(sel ? Palette.PRIMARY : Palette.ON_SURFACE);
                Fonts.sansSmall.draw(batch, (sel ? "> " : "  ") + items.get(i).getName(), itemX, itemY);

                // Stat label, right-aligned within its column
                Fonts.monoTiny.setColor(sel ? Palette.PRIMARY : Palette.SECONDARY);
                float statX = itemX + colW - statLayouts[i].width - PAD;
                Fonts.monoTiny.draw(batch, statLabel(items.get(i)), statX, itemY);
            }
        }

        // Footer hint
        Fonts.monoTiny.setColor(Palette.SECONDARY);
        Fonts.monoTiny.draw(batch, "Z: USAR   X: VOLVER",
            innerX + PAD,
            innerY + FOOTER_H * 0.7f + Fonts.monoTiny.getCapHeight());

        batch.end();
    }
}
