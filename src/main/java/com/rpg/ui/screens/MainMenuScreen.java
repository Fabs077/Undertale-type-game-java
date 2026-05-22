package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.RpgGame;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.PixelButton;
import com.rpg.ui.widgets.PixelButton.Icon;

public class MainMenuScreen extends BaseScreen {

    // ── Button layout ──────────────────────────────────────────────────────
    private static final float BTN_W      = 440f;
    private static final float BTN_H      = 60f;
    private static final float BTN_GAP    = 18f;
    private static final float BTN_X      = (1280 - BTN_W) / 2f;
    private static final float BTN_BASE_Y = 170f;  // EXIT button bottom Y

    // ── Title box ──────────────────────────────────────────────────────────
    private static final float TITLE_BOX_Y  = 430f;
    private static final float TITLE_BOX_H  = 100f;
    private static final float TITLE_BORDER = 4f;

    // ── Crown (battlement above title box) ─────────────────────────────────
    private static final float CROWN_BAR_H   = 12f;
    private static final float CROWN_TOOTH_W = 16f;
    private static final float CROWN_TOOTH_H = 26f;
    private static final int   CROWN_TEETH   = 5;

    private final PixelButton[] buttons;
    private final GlyphLayout   titleLayout;
    private final float         titleBoxW;   // computed from font width + padding
    private final float         titleBoxX;
    private int selectedIndex = 0;

    public MainMenuScreen(RpgGame game) {
        super(game);

        titleLayout = new GlyphLayout(Fonts.sansHuge, "PROYECTO RPG");
        titleBoxW   = titleLayout.width + 80f;
        titleBoxX   = (1280 - titleBoxW) / 2f;

        String[] labels = { "PLAY", "LOAD GAME", "EXIT" };
        Icon[]   icons  = { Icon.PLAY, Icon.DISK, Icon.DOOR };
        buttons = new PixelButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            float y = BTN_BASE_Y + (labels.length - 1 - i) * (BTN_H + BTN_GAP);
            buttons[i] = new PixelButton(BTN_X, y, BTN_W, BTN_H, labels[i], icons[i]);
        }
        buttons[selectedIndex].setSelected(true);
    }

    private void navigate(int dir) {
        buttons[selectedIndex].setSelected(false);
        selectedIndex = (selectedIndex + dir + buttons.length) % buttons.length;
        buttons[selectedIndex].setSelected(true);
    }

    private void confirm() {
        switch (selectedIndex) {
            case 0 -> game.goToDialogue(1);
            case 1 -> game.setScreen(new SaveSlotScreen(game));
            case 2 -> Gdx.app.exit();
        }
    }

    @Override
    protected void draw(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.UP))   navigate(-1);
        if (Gdx.input.isKeyJustPressed(Keys.DOWN))  navigate(+1);
        if (Gdx.input.isKeyJustPressed(Keys.Z))     confirm();

        drawTitleArea();

        for (PixelButton btn : buttons) {
            btn.render(game.shapes, game.batch);
        }
    }

    // ── Title box + crown ──────────────────────────────────────────────────

    private void drawTitleArea() {
        float crownBaseY = TITLE_BOX_Y + TITLE_BOX_H;  // crown sits directly on top of box

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Orange border rectangle
        game.shapes.setColor(Palette.PRIMARY);
        game.shapes.rect(titleBoxX, TITLE_BOX_Y, titleBoxW, TITLE_BOX_H);
        // Dark inner fill
        game.shapes.setColor(Palette.BG);
        game.shapes.rect(
            titleBoxX + TITLE_BORDER, TITLE_BOX_Y + TITLE_BORDER,
            titleBoxW - 2 * TITLE_BORDER, TITLE_BOX_H - 2 * TITLE_BORDER);

        // Crown: full-width horizontal bar
        game.shapes.setColor(Palette.PRIMARY);
        game.shapes.rect(titleBoxX, crownBaseY, titleBoxW, CROWN_BAR_H);

        // Crown: teeth evenly distributed across the bar
        float spacing = titleBoxW / (CROWN_TEETH + 1);
        for (int i = 0; i < CROWN_TEETH; i++) {
            float tx = titleBoxX + spacing * (i + 1) - CROWN_TOOTH_W / 2f;
            game.shapes.rect(tx, crownBaseY + CROWN_BAR_H, CROWN_TOOTH_W, CROWN_TOOTH_H);
        }

        game.shapes.end();

        // Title text — centered in the box
        game.batch.begin();
        Fonts.sansHuge.setColor(Palette.PRIMARY);
        float textX = titleBoxX + (titleBoxW - titleLayout.width) / 2f;
        float textY = TITLE_BOX_Y + (TITLE_BOX_H + titleLayout.height) / 2f;
        Fonts.sansHuge.draw(game.batch, titleLayout, textX, textY);
        game.batch.end();
    }
}
