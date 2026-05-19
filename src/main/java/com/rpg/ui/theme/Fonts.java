package com.rpg.ui.theme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public final class Fonts {
    public static BitmapFont sansHuge;    // DTM-Sans 72
    public static BitmapFont sansLarge;   // DTM-Sans 32
    public static BitmapFont sansMedium;  // DTM-Sans 24
    public static BitmapFont sansSmall;   // DTM-Sans 18

    public static BitmapFont monoMedium;  // DTM-Mono 24
    public static BitmapFont monoSmall;   // DTM-Mono 18
    public static BitmapFont monoTiny;    // DTM-Mono 14

    public static void load() {
        FreeTypeFontGenerator sans = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DTM-Sans.otf"));
        sansHuge   = generate(sans, 72);
        sansLarge  = generate(sans, 32);
        sansMedium = generate(sans, 24);
        sansSmall  = generate(sans, 18);
        sans.dispose();

        FreeTypeFontGenerator mono = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DTM-Mono.otf"));
        monoMedium = generate(mono, 24);
        monoSmall  = generate(mono, 18);
        monoTiny   = generate(mono, 14);
        mono.dispose();
    }

    private static BitmapFont generate(FreeTypeFontGenerator gen, int size) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size      = size;
        p.minFilter = TextureFilter.Nearest;
        p.magFilter = TextureFilter.Nearest;
        return gen.generateFont(p);
    }

    public static void dispose() {
        sansHuge.dispose();
        sansLarge.dispose();
        sansMedium.dispose();
        sansSmall.dispose();
        monoMedium.dispose();
        monoSmall.dispose();
        monoTiny.dispose();
    }

    private Fonts() {}
}
