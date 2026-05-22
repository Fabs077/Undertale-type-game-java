package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

public class PixelButton {

    public enum Icon { NONE, PLAY, DISK, DOOR }

    private static final int   BORDER    = 3;
    private static final float ICON_SIZE = 20f;  // reserved width for icon area
    private static final float ICON_GAP  = 10f;  // gap between icon and label

    private final float   x, y, width, height;
    private final String  label;
    private final Icon    icon;
    private final float   textH;     // cached ascent for vertical centering
    private final float   contentW;  // total width of icon+gap+text cluster
    private       boolean selected;

    /** Backward-compatible constructor — no icon. Used by ActionMenu, SaveExitDialog, etc. */
    public PixelButton(float x, float y, float width, float height, String label) {
        this(x, y, width, height, label, Icon.NONE);
    }

    public PixelButton(float x, float y, float width, float height, String label, Icon icon) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.label  = label;
        this.icon   = icon;
        GlyphLayout lay = new GlyphLayout(Fonts.sansMedium, label);
        this.textH    = lay.height;
        float textW   = lay.width;
        this.contentW = (icon != Icon.NONE) ? ICON_SIZE + ICON_GAP + textW : textW;
    }

    public void render(ShapeRenderer shapes, SpriteBatch batch) {
        Color fg   = selected ? Palette.PRIMARY      : Palette.ON_SURFACE;
        Color fill = selected ? Palette.SURFACE_WARM : Palette.BG;

        float startX = x + (width - contentW) / 2f;
        float iconY  = y + (height - ICON_SIZE) / 2f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Border
        shapes.setColor(fg);
        shapes.rect(x, y, width, height);
        // Inner fill (warm dark orange when selected, pure black otherwise)
        shapes.setColor(fill);
        shapes.rect(x + BORDER, y + BORDER, width - 2f * BORDER, height - 2f * BORDER);
        // Icon
        if (icon != Icon.NONE) {
            shapes.setColor(fg);
            drawIcon(shapes, startX, iconY, fg);
        }
        shapes.end();

        batch.begin();
        float textX = (icon != Icon.NONE) ? startX + ICON_SIZE + ICON_GAP : startX;
        float textY = y + (height + textH) / 2f;
        Fonts.sansMedium.setColor(fg);
        Fonts.sansMedium.draw(batch, label, textX, textY);
        batch.end();
    }

    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected()               { return selected; }
    public String  getLabel()                 { return label; }

    // ── Icon drawing ──────────────────────────────────────────────────────────

    private void drawIcon(ShapeRenderer s, float ix, float iy, Color fg) {
        switch (icon) {
            case PLAY -> drawPlay(s, ix, iy);
            case DISK -> drawDisk(s, ix, iy, fg);
            case DOOR -> drawDoor(s, ix, iy, fg);
            default   -> { }
        }
    }

    // ▶ Right-pointing pixel triangle — flat left edge, point on right (4px blocks, 12×20)
    private static void drawPlay(ShapeRenderer s, float ix, float iy) {
        int b = 4;
        s.rect(ix, iy,         b, b);   // bottom tip
        s.rect(ix, iy +   b, 2*b, b);
        s.rect(ix, iy + 2*b, 3*b, b);   // widest (the point)
        s.rect(ix, iy + 3*b, 2*b, b);
        s.rect(ix, iy + 4*b,   b, b);   // top tip
    }

    // Floppy disk — orange border + label strip at top + dark inner + notch
    private static void drawDisk(ShapeRenderer s, float ix, float iy, Color fg) {
        s.rect(ix, iy, 20, 20);                  // full square in fg
        s.setColor(Palette.BG);
        s.rect(ix + 3, iy + 3, 14, 10);          // inner cutout (below label)
        s.rect(ix + 6, iy + 15, 8, 3);           // slot on the label strip
        s.setColor(fg);
    }

    // Exit door — frame + doorway opening at bottom + knob
    private static void drawDoor(ShapeRenderer s, float ix, float iy, Color fg) {
        s.rect(ix, iy, 18, 20);                  // full frame in fg
        s.setColor(Palette.BG);
        s.rect(ix + 3, iy, 12, 16);              // doorway opening (bottom cutout)
        s.setColor(fg);
        s.rect(ix + 12, iy + 7, 3, 3);           // door knob
    }
}
