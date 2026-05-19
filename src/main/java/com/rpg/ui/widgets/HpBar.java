package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

public class HpBar {

    private static final float BAR_W = 180f;
    private static final float BAR_H = 18f;

    private int level, hp, maxHp;
    private GlyphLayout leftLayout;   // "LV 1   HP   "  — mono18
    private GlyphLayout rightLayout;  // " 20 / 20"      — mono14

    public HpBar(int level, int hp, int maxHp) {
        this.level  = level;
        this.hp     = hp;
        this.maxHp  = maxHp;
        rebuildLayouts();
    }

    private void rebuildLayouts() {
        leftLayout  = new GlyphLayout(Fonts.monoSmall, "LV " + level + "   HP   ");
        rightLayout = new GlyphLayout(Fonts.monoTiny,  " " + hp + " / " + maxHp);
    }

    public void setHp(int hp)       { this.hp    = hp;    rebuildLayouts(); }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; rebuildLayouts(); }

    /** Total pixel width of the full widget — use for horizontal centering. */
    public float getTotalWidth() {
        return leftLayout.width + BAR_W + rightLayout.width;
    }

    /**
     * Render at (x, y) bottom-left.
     * Shapes pass first (bar rects), then batch pass (text).
     */
    public void render(ShapeRenderer shapes, SpriteBatch batch, float x, float y) {
        float barX  = x + leftLayout.width;
        float fillW = maxHp > 0 ? (float) hp / maxHp * BAR_W : 0f;
        float midY  = y + BAR_H / 2f;

        // ── Shapes: gray background bar + yellow/orange fill ──
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Palette.SECONDARY);  // gray background
        shapes.rect(barX, y, BAR_W, BAR_H);
        shapes.setColor(Palette.PRIMARY);    // orange/yellow fill
        shapes.rect(barX, y, fillW, BAR_H);
        shapes.end();

        // ── Batch: "LV 1   HP   " (mono18) + " 20 / 20" (mono14) ──
        batch.begin();
        Fonts.monoSmall.setColor(Palette.ON_SURFACE);
        Fonts.monoSmall.draw(batch, leftLayout,  x,            midY + leftLayout.height  / 2f);
        Fonts.monoTiny.setColor(Palette.ON_SURFACE);
        Fonts.monoTiny.draw(batch,  rightLayout, barX + BAR_W, midY + rightLayout.height / 2f);
        batch.end();
    }
}
