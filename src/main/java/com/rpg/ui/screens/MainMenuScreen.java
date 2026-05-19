package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.rpg.ui.RpgGame;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.PixelButton;

public class MainMenuScreen extends BaseScreen {

    private static final float BTN_W   = 360f;
    private static final float BTN_H   = 56f;
    private static final float BTN_GAP = 24f;
    private static final float BTN_X   = (1280 - BTN_W) / 2f;
    // Bottommost button starts here; buttons stack upward
    private static final float BTN_BASE_Y = 302f;
    private static final float TITLE_Y    = 630f;

    private final PixelButton[] buttons;
    private final GlyphLayout titleLayout;
    private final float titleX;
    private int selectedIndex = 0;

    public MainMenuScreen(RpgGame game) {
        super(game);

        titleLayout = new GlyphLayout(Fonts.sansHuge, "PROYECTO RPG");
        titleX = (1280 - titleLayout.width) / 2f;

        String[] labels = { "PLAY", "LOAD GAME", "EXIT" };
        buttons = new PixelButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            float y = BTN_BASE_Y + (labels.length - 1 - i) * (BTN_H + BTN_GAP);
            buttons[i] = new PixelButton(BTN_X, y, BTN_W, BTN_H, labels[i]);
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

        game.batch.begin();
        Fonts.sansHuge.setColor(Palette.PRIMARY);
        Fonts.sansHuge.draw(game.batch, titleLayout, titleX, TITLE_Y);
        game.batch.end();

        for (PixelButton btn : buttons) {
            btn.render(game.shapes, game.batch);
        }
    }
}
