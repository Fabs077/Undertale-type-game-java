package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.RpgGame;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.PixelButton;

public class VictoryScreen extends BaseScreen {

    private static final float TITLE_Y      = 590f;
    private static final float PANEL_X      = 400f;
    private static final float PANEL_Y      = 320f;
    private static final float PANEL_W      = 480f;
    private static final float PANEL_H      = 140f;
    private static final float BORDER       = 2f;
    private static final float PAD          = 20f;
    private static final float BTN_W        = 400f;
    private static final float BTN_H        = 56f;
    private static final float BTN_Y        = 80f;

    private final GlyphLayout  titleLayout;
    private final float        titleX;
    private final PixelButton  returnButton;
    private final GlyphLayout  sparedLayout;
    private final GlyphLayout  defeatedLayout;

    public VictoryScreen(RpgGame game) {
        super(game);

        titleLayout    = new GlyphLayout(Fonts.sansHuge, "THE END");
        titleX         = (1280 - titleLayout.width) / 2f;

        sparedLayout   = new GlyphLayout(Fonts.sansMedium, "Spared: 3");
        defeatedLayout = new GlyphLayout(Fonts.sansMedium, "Defeated: 2");

        float btnX   = (1280 - BTN_W) / 2f;
        returnButton = new PixelButton(btnX, BTN_Y, BTN_W, BTN_H, "RETURN TO MENU");
        returnButton.setSelected(true);
    }

    @Override
    protected void draw(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            game.setScreen(new MainMenuScreen(game));
        }

        game.batch.begin();
        Fonts.sansHuge.setColor(Palette.PRIMARY);
        Fonts.sansHuge.draw(game.batch, titleLayout, titleX, TITLE_Y);
        game.batch.end();

        // Summary panel
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(Palette.ON_SURFACE);
        game.shapes.rect(PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        game.shapes.setColor(Palette.BG);
        game.shapes.rect(PANEL_X + BORDER, PANEL_Y + BORDER, PANEL_W - 2 * BORDER, PANEL_H - 2 * BORDER);
        game.shapes.end();

        float lineH = Fonts.sansMedium.getLineHeight();
        float textX = PANEL_X + PAD;
        float topY  = PANEL_Y + PANEL_H - PAD;

        game.batch.begin();
        Fonts.sansMedium.setColor(Palette.ON_SURFACE);
        Fonts.sansMedium.draw(game.batch, sparedLayout,   textX, topY);
        Fonts.sansMedium.draw(game.batch, defeatedLayout, textX, topY - lineH - 8f);
        game.batch.end();

        returnButton.render(game.shapes, game.batch);
    }
}
