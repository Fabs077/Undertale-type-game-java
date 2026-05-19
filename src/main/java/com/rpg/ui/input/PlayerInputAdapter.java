package com.rpg.ui.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public final class PlayerInputAdapter {

    public boolean up()      { return Gdx.input.isKeyJustPressed(Keys.UP);    }
    public boolean down()    { return Gdx.input.isKeyJustPressed(Keys.DOWN);  }
    public boolean left()    { return Gdx.input.isKeyJustPressed(Keys.LEFT);  }
    public boolean right()   { return Gdx.input.isKeyJustPressed(Keys.RIGHT); }
    public boolean confirm() { return Gdx.input.isKeyJustPressed(Keys.Z);      }
    public boolean cancel()  { return Gdx.input.isKeyJustPressed(Keys.X);      }
    public boolean escape()  { return Gdx.input.isKeyJustPressed(Keys.ESCAPE); }
}
