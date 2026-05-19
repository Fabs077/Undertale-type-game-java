package com.rpg.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.screens.CombatScreen;
import com.rpg.ui.screens.DialogueScreen;
import com.rpg.ui.screens.GameOverScreen;
import com.rpg.ui.screens.MainMenuScreen;
import com.rpg.ui.screens.VictoryScreen;
import com.rpg.ui.theme.Fonts;

public class RpgGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapes;

    @Override
    public void create() {
        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();
        Fonts.load();
        setScreen(new MainMenuScreen(this));
    }

    public void goToDialogue(int phase) { setScreen(new DialogueScreen(this, phase)); }
    public void goToCombat()            { setScreen(new CombatScreen(this)); }
    public void goToGameOver()          { setScreen(new GameOverScreen(this)); }
    public void goToVictory()           { setScreen(new VictoryScreen(this)); }
    public void goToMainMenu()          { setScreen(new MainMenuScreen(this)); }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
        shapes.dispose();
        Fonts.dispose();
        super.dispose();
    }
}
