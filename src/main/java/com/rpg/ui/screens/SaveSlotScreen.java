package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.entities.Player;
import com.rpg.engine.procedural.HistoryManager;
import com.rpg.ui.RpgGame;
import com.rpg.ui.bridge.CombatController;
import com.rpg.ui.bridge.SaveBundle;
import com.rpg.ui.bridge.SaveSlotManager;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.SaveSlotCard;

public class SaveSlotScreen extends BaseScreen {

    public enum Mode { LOAD, SAVE }

    private static final int   SLOT_COUNT  = 3;
    private static final float CARD_X      = (1280 - SaveSlotCard.WIDTH) / 2f;
    private static final float CARD_GAP    = 24f;
    private static final float STACK_H     = SLOT_COUNT * SaveSlotCard.HEIGHT + (SLOT_COUNT - 1) * CARD_GAP;
    private static final float CARD_BASE_Y = (720 - STACK_H) / 2f;
    private static final float TITLE_Y     = 660f;

    private final Mode           mode;
    private final CombatController controller; // null en modo LOAD
    private final SaveSlotCard[] cards;
    private final SaveSlotManager manager;
    private final GlyphLayout    titleLayout;
    private final float          titleX;
    private int selectedIndex = 0;

    /** Modo LOAD: desde el menú principal. */
    public SaveSlotScreen(RpgGame game) {
        this(game, Mode.LOAD, null);
    }

    /** Modo SAVE: desde PostBossScreen después de derrotar un boss. */
    public SaveSlotScreen(RpgGame game, CombatController controller) {
        this(game, Mode.SAVE, controller);
    }

    private SaveSlotScreen(RpgGame game, Mode mode, CombatController controller) {
        super(game);
        this.mode       = mode;
        this.controller = controller;
        this.manager    = new SaveSlotManager();

        String title = (mode == Mode.SAVE) ? "GUARDAR PARTIDA" : "CARGAR PARTIDA";
        titleLayout = new GlyphLayout(Fonts.sansLarge, title);
        titleX      = (1280 - titleLayout.width) / 2f;

        cards = new SaveSlotCard[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            float y = CARD_BASE_Y + (SLOT_COUNT - 1 - i) * (SaveSlotCard.HEIGHT + CARD_GAP);
            cards[i] = new SaveSlotCard(i, CARD_X, y);
            cards[i].setInfo(manager.getSlotInfo(i));
        }
        cards[selectedIndex].setSelected(true);
    }

    private void navigate(int dir) {
        cards[selectedIndex].setSelected(false);
        selectedIndex = (selectedIndex + dir + SLOT_COUNT) % SLOT_COUNT;
        cards[selectedIndex].setSelected(true);
    }

    private void confirm() {
        if (mode == Mode.SAVE) {
            executeSave();
        } else {
            executeLoad();
        }
    }

    private void executeSave() {
        try {
            manager.saveToSlot(selectedIndex,
                    controller.getPlayer(),
                    controller.getHistoryManager(),
                    controller.getPhase());
            game.goToMainMenu();
        } catch (SaveCorruptionException e) {
            cards[selectedIndex].setCorrupted(true);
        }
    }

    private void executeLoad() {
        if (cards[selectedIndex].isCorrupted()) return;

        try {
            SaveBundle bundle = manager.loadBundle(selectedIndex);

            Player player = new Player("?", 1, 1, 1);
            player.loadData(bundle.playerJson());

            HistoryManager history = new HistoryManager();
            history.loadData(bundle.historyJson());

            CombatController loaded = new CombatController(player, history, bundle.phase());
            game.setScreen(new CombatScreen(game, loaded));

        } catch (SaveCorruptionException e) {
            cards[selectedIndex].setCorrupted(true);
            cards[selectedIndex].setSelected(false);
        }
    }

    @Override
    protected void draw(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.UP))     navigate(-1);
        if (Gdx.input.isKeyJustPressed(Keys.DOWN))   navigate(+1);
        if (Gdx.input.isKeyJustPressed(Keys.Z))      confirm();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) game.goToMainMenu();

        game.batch.begin();
        Fonts.sansLarge.setColor(Palette.PRIMARY);
        Fonts.sansLarge.draw(game.batch, titleLayout, titleX, TITLE_Y);
        game.batch.end();

        for (SaveSlotCard card : cards) card.render(game.shapes, game.batch);
    }
}
