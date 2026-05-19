package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Palette;

public class CombatBox {

    public enum Mode { NARROW, WIDE }

    // NARROW: taller, for dialog text. WIDE: wider and shorter, for bullet hell.
    private static final float NARROW_W = 640f;
    private static final float NARROW_H = 190f;
    private static final float WIDE_W   = 300f;
    private static final float WIDE_H   = 300f;
    private static final int   BORDER   = 4;

    private final float centerX;
    private final float centerY;
    private Mode mode = Mode.NARROW;

    public CombatBox(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void toggleMode() {
        mode = (mode == Mode.NARROW) ? Mode.WIDE : Mode.NARROW;
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode  getMode()         { return mode; }

    public float getWidth()  { return mode == Mode.NARROW ? NARROW_W : WIDE_W; }
    public float getHeight() { return mode == Mode.NARROW ? NARROW_H : WIDE_H; }
    public float getX()      { return centerX - getWidth()  / 2f; }
    public float getY()      { return centerY - getHeight() / 2f; }

    public float getInnerX()      { return getX()      + BORDER; }
    public float getInnerY()      { return getY()      + BORDER; }
    public float getInnerWidth()  { return getWidth()  - 2 * BORDER; }
    public float getInnerHeight() { return getHeight() - 2 * BORDER; }

    public void render(ShapeRenderer shapes) {
        float w = getWidth();
        float h = getHeight();
        float x = getX();
        float y = getY();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Palette.ON_SURFACE);
        shapes.rect(x, y, w, h);
        shapes.setColor(Palette.BG);
        shapes.rect(x + BORDER, y + BORDER, w - 2 * BORDER, h - 2 * BORDER);
        shapes.end();
    }
}
