package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ActionMenu {

    private static final String[] LABELS   = { "FIGHT", "ACT", "ITEM", "MERCY" };
    private static final float    BTN_H    = 72f;
    private static final float    BTN_GAP  = 24f;
    private static final float    MARGIN_X = 40f;
    private static final float    SCREEN_W = 1280f;

    private final PixelButton[] buttons;
    private int selectedIndex = 0;

    public ActionMenu(float y) {
        float totalGaps = (LABELS.length - 1) * BTN_GAP;
        float btnW = (SCREEN_W - 2f * MARGIN_X - totalGaps) / LABELS.length;

        buttons = new PixelButton[LABELS.length];
        for (int i = 0; i < LABELS.length; i++) {
            float bx = MARGIN_X + i * (btnW + BTN_GAP);
            buttons[i] = new PixelButton(bx, y, btnW, BTN_H, LABELS[i]);
        }
        buttons[selectedIndex].setSelected(true);
    }

    public void navigate(int dir) {
        buttons[selectedIndex].setSelected(false);
        selectedIndex = Math.floorMod(selectedIndex + dir, LABELS.length);
        buttons[selectedIndex].setSelected(true);
    }

    public int    getSelectedIndex() { return selectedIndex; }
    public String getSelectedLabel() { return LABELS[selectedIndex]; }

    public void render(ShapeRenderer shapes, SpriteBatch batch) {
        for (PixelButton btn : buttons) {
            btn.render(shapes, batch);
        }
    }
}
