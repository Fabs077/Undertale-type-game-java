package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.ui.RpgGame;
import com.rpg.ui.bridge.SaveSlotManager;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.SaveSlotCard;

public class SaveSlotScreen extends BaseScreen {

    private static final int   SLOT_COUNT  = 3;
    private static final float CARD_X      = (1280 - SaveSlotCard.WIDTH) / 2f;
    private static final float CARD_GAP    = 24f;
    private static final float STACK_H     = SLOT_COUNT * SaveSlotCard.HEIGHT + (SLOT_COUNT - 1) * CARD_GAP;
    private static final float CARD_BASE_Y = (720 - STACK_H) / 2f;
    private static final float TITLE_Y     = 660f;

    private final SaveSlotCard[]   cards;
    private final SaveSlotManager  manager;
    private final GlyphLayout      titleLayout;
    private final float            titleX;
    private int selectedIndex = 0;

    public SaveSlotScreen(RpgGame game) {
        super(game);
        manager     = new SaveSlotManager();
        titleLayout = new GlyphLayout(Fonts.sansLarge, "CARGAR PARTIDA");
        titleX      = (1280 - titleLayout.width) / 2f;

        cards = new SaveSlotCard[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            // Stack top-to-bottom: index 0 is the topmost card (highest y)
            float y = CARD_BASE_Y + (SLOT_COUNT - 1 - i) * (SaveSlotCard.HEIGHT + CARD_GAP);
            cards[i] = new SaveSlotCard(i, CARD_X, y);
        }
        cards[selectedIndex].setSelected(true);
    }

    private void navigate(int dir) {
        cards[selectedIndex].setSelected(false);
        selectedIndex = (selectedIndex + dir + SLOT_COUNT) % SLOT_COUNT;
        cards[selectedIndex].setSelected(true);
    }

    private void confirm() {
        if (cards[selectedIndex].isCorrupted()) return;
        try {
            String json = manager.loadRawJson(selectedIndex);
            System.out.println("Slot " + selectedIndex + " cargado OK: " + json.length() + " bytes");
        } catch (SaveCorruptionException e) {
            cards[selectedIndex].setCorrupted(true);
            cards[selectedIndex].setSelected(false);
        }
    }

    @Override
    protected void draw(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.UP))    navigate(-1);
        if (Gdx.input.isKeyJustPressed(Keys.DOWN))  navigate(+1);
        if (Gdx.input.isKeyJustPressed(Keys.Z))     confirm();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) game.setScreen(new MainMenuScreen(game));

        game.batch.begin();
        Fonts.sansLarge.setColor(Palette.PRIMARY);
        Fonts.sansLarge.draw(game.batch, titleLayout, titleX, TITLE_Y);
        game.batch.end();

        for (SaveSlotCard card : cards) {
            card.render(game.shapes, game.batch);
        }
    }
}
