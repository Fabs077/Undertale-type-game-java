package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.rpg.ui.theme.Palette;

public class CombatBox {

    public enum Mode { NARROW, WIDE }

    private static final float NARROW_W    = 640f;
    private static final float NARROW_H    = 190f;
    private static final float WIDE_W      = 300f;
    private static final float WIDE_H      = 300f;
    // Phase 2 (Blue Soul / van traffic) — wide and short so jumping feels natural
    static final float WIDE_P2_W  = 480f;
    static final float WIDE_P2_H  = 180f;
    private static final int   BORDER     = 4;
    private static final float LERP_SPEED = 10f;  // higher = snappier animation

    private final float centerX;
    private final float centerY;
    private Mode  mode;

    private float currentW;
    private float currentH;
    private float targetW;
    private float targetH;

    public CombatBox(float centerX, float centerY) {
        this.centerX  = centerX;
        this.centerY  = centerY;
        this.mode     = Mode.NARROW;
        this.currentW = NARROW_W;
        this.currentH = NARROW_H;
        this.targetW  = NARROW_W;
        this.targetH  = NARROW_H;
    }

    /** Lerps currentW/H toward target. Call once per frame in CombatScreen.draw(). */
    public void update(float delta) {
        float t = Math.min(1f, LERP_SPEED * delta);
        currentW = MathUtils.lerp(currentW, targetW, t);
        currentH = MathUtils.lerp(currentH, targetH, t);
        // Snap to avoid perpetual micro-drift
        if (Math.abs(currentW - targetW) < 0.5f) currentW = targetW;
        if (Math.abs(currentH - targetH) < 0.5f) currentH = targetH;
    }

    public void setMode(Mode mode) {
        this.mode    = mode;
        this.targetW = (mode == Mode.NARROW) ? NARROW_W : WIDE_W;
        this.targetH = (mode == Mode.NARROW) ? NARROW_H : WIDE_H;
    }

    /** Overrides the target to the Phase 2 horizontal arena (wider and shorter). */
    public void setPhase2Shape() {
        targetW = WIDE_P2_W;
        targetH = WIDE_P2_H;
    }

    /** Immediately jumps currentW/H to the target — use before spawning souls or patterns. */
    public void snapToTarget() {
        currentW = targetW;
        currentH = targetH;
    }

    public Mode getMode() { return mode; }

    public float getWidth()  { return currentW; }
    public float getHeight() { return currentH; }
    public float getX()      { return centerX - currentW / 2f; }
    public float getY()      { return centerY - currentH / 2f; }

    public float getInnerX()      { return getX()      + BORDER; }
    public float getInnerY()      { return getY()      + BORDER; }
    public float getInnerWidth()  { return getWidth()  - 2 * BORDER; }
    public float getInnerHeight() { return getHeight() - 2 * BORDER; }

    public void render(ShapeRenderer shapes) {
        float w = currentW;
        float h = currentH;
        float x = centerX - w / 2f;
        float y = centerY - h / 2f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Palette.ON_SURFACE);
        shapes.rect(x, y, w, h);
        shapes.setColor(Palette.BG);
        shapes.rect(x + BORDER, y + BORDER, w - 2 * BORDER, h - 2 * BORDER);
        shapes.end();
    }
}
