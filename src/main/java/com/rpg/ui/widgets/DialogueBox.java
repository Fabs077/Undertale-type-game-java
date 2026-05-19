package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

public class DialogueBox {

    private static final float CHARS_PER_SEC = 30f;
    private static final float PAD           = 10f;
    private static final float BLINK_HALF    = 0.45f;
    private static final float TRI_W         = 14f;
    private static final float TRI_H         = 8f;

    private String         fullText     = "";
    private float          elapsed      = 0f;
    private int            visibleChars = 0;
    private boolean        done         = false;
    private float          blinkTimer   = 0f;
    private boolean        blinkVisible = true;
    private int            lastRendered = -1;
    private final GlyphLayout layout   = new GlyphLayout();

    public void setText(String text) {
        this.fullText     = text == null ? "" : text;
        this.elapsed      = 0f;
        this.visibleChars = 0;
        this.done         = false;
        this.blinkTimer   = 0f;
        this.blinkVisible = true;
        this.lastRendered = -1;
    }

    /** Instantly reveal all characters (Z pressed mid-typewriter). */
    public void skip() {
        visibleChars = fullText.length();
        done = true;
    }

    public boolean isDone() { return done; }

    public void update(float delta) {
        if (!done) {
            elapsed += delta;
            int next = Math.min(fullText.length(), (int) (elapsed * CHARS_PER_SEC));
            visibleChars = next;
            if (visibleChars >= fullText.length()) done = true;
        } else {
            blinkTimer += delta;
            if (blinkTimer >= BLINK_HALF) {
                blinkTimer -= BLINK_HALF;
                blinkVisible = !blinkVisible;
            }
        }
    }

    /**
     * Render inside the CombatBox inner area (already accounting for the 4px border).
     * innerX/Y is bottom-left of the drawable region in libGDX y-up coordinates.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapes,
                       float innerX, float innerY, float innerW, float innerH) {
        if (fullText.isEmpty()) return;

        float maxW     = innerW - 2f * PAD;
        float textX    = innerX + PAD;
        // libGDX y-up: font.draw y = top of first ascender line
        float textTopY = innerY + innerH - PAD;

        // Rebuild GlyphLayout only when the visible slice changes
        if (visibleChars != lastRendered) {
            String visible = fullText.substring(0, visibleChars);
            layout.setText(Fonts.sansMedium, visible, Palette.ON_SURFACE, maxW, Align.left, true);
            lastRendered = visibleChars;
        }

        batch.begin();
        Fonts.sansMedium.draw(batch, layout, textX, textTopY);
        batch.end();

        // Blinking inverted triangle at bottom-right corner when typewriter is done
        if (done && blinkVisible) {
            float triCx = innerX + innerW - PAD - TRI_W / 2f;
            float triTopY = innerY + PAD + TRI_H;  // top edge of triangle
            float triTipY = innerY + PAD;           // downward tip
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Palette.ON_SURFACE);
            shapes.triangle(
                triCx - TRI_W / 2f, triTopY,   // top-left vertex
                triCx + TRI_W / 2f, triTopY,   // top-right vertex
                triCx,              triTipY     // bottom tip (pointing down)
            );
            shapes.end();
        }
    }
}
