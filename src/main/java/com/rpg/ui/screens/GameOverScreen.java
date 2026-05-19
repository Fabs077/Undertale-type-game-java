package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.rpg.ui.RpgGame;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.PixelButton;

public class GameOverScreen extends BaseScreen {

    private static final float TITLE_Y      = 540f;
    private static final float BTN_W        = 280f;
    private static final float BTN_H        = 56f;
    private static final float BTN_GAP      = 40f;
    private static final float BTN_Y        = 120f;
    private static final float BTNS_TOTAL_W = BTN_W * 2 + BTN_GAP;
    private static final float BTN_START_X  = (1280 - BTNS_TOTAL_W) / 2f;

    private final PixelButton[] buttons;
    private final GlyphLayout   titleLayout;
    private final float         titleX;
    private int selectedIndex = 0;

    public GameOverScreen(RpgGame game) {
        super(game);

        titleLayout = new GlyphLayout(Fonts.sansHuge, "GAME OVER");
        titleX      = (1280 - titleLayout.width) / 2f;

        buttons = new PixelButton[] {
            new PixelButton(BTN_START_X,                    BTN_Y, BTN_W, BTN_H, "RETRY"),
            new PixelButton(BTN_START_X + BTN_W + BTN_GAP, BTN_Y, BTN_W, BTN_H, "MENU")
        };
        buttons[selectedIndex].setSelected(true);
    }

    private void navigate(int dir) {
        buttons[selectedIndex].setSelected(false);
        selectedIndex = (selectedIndex + dir + buttons.length) % buttons.length;
        buttons[selectedIndex].setSelected(true);
    }

    private void confirm() {
        switch (selectedIndex) {
            case 0 -> game.setScreen(new CombatScreen(game));
            case 1 -> game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    protected void draw(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.LEFT))  navigate(-1);
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) navigate(+1);
        if (Gdx.input.isKeyJustPressed(Keys.Z))     confirm();

        game.batch.begin();
        Fonts.sansHuge.setColor(Palette.ERROR);
        Fonts.sansHuge.draw(game.batch, titleLayout, titleX, TITLE_Y);
        game.batch.end();

        for (PixelButton btn : buttons) {
            btn.render(game.shapes, game.batch);
        }
    }
}
