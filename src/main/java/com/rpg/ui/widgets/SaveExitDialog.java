package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

/**
 * Full-screen modal pause dialog.
 * Dims the screen, shows a question box, and two buttons: SAVE & EXIT / CONTINUE.
 * Caller is responsible for stopping game logic (not calling controller.tick) while active.
 */
public class SaveExitDialog {

    // Text box — centred on screen (1280×720)
    private static final float TXT_W    = 540f;
    private static final float TXT_H    = 110f;
    private static final float TXT_X    = (1280f - TXT_W) / 2f;  // 370
    private static final float TXT_Y    = 360f - TXT_H / 2f + 50f; // 345
    private static final float TXT_BDR  = 4f;
    private static final float TXT_PAD  = 14f;

    // Button row — below text box
    private static final float BTN_GAP  = 20f;
    private static final float BTN_H    = 60f;
    private static final float BTN_W    = (TXT_W - BTN_GAP) / 2f; // 260
    private static final float BTN_Y    = TXT_Y - BTN_H - 14f;
    private static final float BTN1_X   = TXT_X;
    private static final float BTN2_X   = TXT_X + BTN_W + BTN_GAP;

    private static final String QUESTION =
        "* Would you like to SAVE and EXIT or CONTINUE the battle?";
    private static final String[] LABELS = { "SAVE & EXIT", "CONTINUE" };

    private final PixelButton[] buttons;
    private final GlyphLayout   questionLayout;
    private int selectedIndex = 1; // default: CONTINUE

    public SaveExitDialog() {
        buttons = new PixelButton[] {
            new PixelButton(BTN1_X, BTN_Y, BTN_W, BTN_H, LABELS[0]),
            new PixelButton(BTN2_X, BTN_Y, BTN_W, BTN_H, LABELS[1]),
        };
        buttons[selectedIndex].setSelected(true);

        // Pre-wrap the question text
        questionLayout = new GlyphLayout();
        questionLayout.setText(Fonts.sansSmall, QUESTION,
            Palette.ON_SURFACE, TXT_W - 2 * TXT_BDR - 2 * TXT_PAD, Align.left, true);
    }

    /** Horizontal navigation between the two buttons. */
    public void navigate(int dx) {
        buttons[selectedIndex].setSelected(false);
        selectedIndex = Math.floorMod(selectedIndex + dx, LABELS.length);
        buttons[selectedIndex].setSelected(true);
    }

    /** Returns true when SAVE & EXIT is highlighted and Z is pressed. */
    public boolean isSaveExitSelected() { return selectedIndex == 0; }

    /** Reset to default (CONTINUE focused) on every open. */
    public void resetSelection() {
        buttons[selectedIndex].setSelected(false);
        selectedIndex = 1;
        buttons[selectedIndex].setSelected(true);
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes) {
        // Full-screen black overlay (solid — matches the mockup)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Palette.BG);
        shapes.rect(0f, 0f, 1280f, 720f);

        // Question box background + 4px white border
        shapes.setColor(Palette.ON_SURFACE);
        shapes.rect(TXT_X, TXT_Y, TXT_W, TXT_H);
        shapes.setColor(Palette.BG);
        shapes.rect(TXT_X + TXT_BDR, TXT_Y + TXT_BDR,
                    TXT_W - 2 * TXT_BDR, TXT_H - 2 * TXT_BDR);
        shapes.end();

        // Question text (word-wrapped, top-left of inner box)
        batch.begin();
        Fonts.sansSmall.setColor(Palette.ON_SURFACE);
        Fonts.sansSmall.draw(batch, questionLayout,
            TXT_X + TXT_BDR + TXT_PAD,
            TXT_Y + TXT_H - TXT_BDR - TXT_PAD);
        batch.end();

        // Buttons
        for (PixelButton btn : buttons) btn.render(shapes, batch);
    }
}
