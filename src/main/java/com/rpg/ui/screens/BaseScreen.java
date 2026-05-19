package com.rpg.ui.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.rpg.ui.RpgGame;
import com.rpg.ui.theme.Palette;

public abstract class BaseScreen implements Screen {
    protected final RpgGame game;
    protected final OrthographicCamera camera;

    protected BaseScreen(RpgGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720); // origin bottom-left, center at (640,360)
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Palette.BG);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapes.setProjectionMatrix(camera.combined);
        draw(delta);
    }

    protected abstract void draw(float delta);

    @Override public void resize(int width, int height) {}
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void dispose() {}
}
