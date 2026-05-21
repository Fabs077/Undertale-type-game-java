package com.rpg.ui.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga y reproduce las animaciones del boss activo.
 *
 * Convención de archivos (todos en sprites/bosses/{spriteId}/):
 *   idle.png    — loop continuo (obligatorio)
 *   hurt.png    — se reproduce una vez al recibir daño, luego vuelve a idle
 *   attack.png  — se reproduce una vez al iniciar el bullet hell, luego vuelve a idle
 *
 * Formato de cada PNG: frames del mismo tamaño en fila horizontal.
 * Los archivos que no existen se omiten sin crash.
 *
 * Uso:
 *   BossAssets assets = new BossAssets("sombra", 64, 64, 0.12f);
 *   // en update: assets.update(delta)
 *   // en render: assets.render(batch, x, y, w, h)
 *   // al recibir golpe: assets.playOnce("hurt")
 *   // al atacar:        assets.playOnce("attack")
 */
public class BossAssets implements BossRenderer {

    private static final String[] KNOWN_ANIMS = { "idle", "hurt", "attack" };

    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final List<Texture> textures = new ArrayList<>();

    private String current  = "idle";
    private String fallback = "idle";   // animación a la que volver tras playOnce
    private float  stateTime   = 0f;
    private boolean playingOnce = false;

    /**
     * @param spriteId      id del boss — determina la subcarpeta (ej. "sombra")
     * @param frameW        ancho de cada frame en el spritesheet (px)
     * @param frameH        alto de cada frame en el spritesheet (px)
     * @param frameDuration segundos por frame
     */
    public BossAssets(String spriteId, int frameW, int frameH, float frameDuration) {
        for (String name : KNOWN_ANIMS) {
            String path = "sprites/bosses/" + spriteId + "/" + name + ".png";
            if (!Gdx.files.internal(path).exists()) continue;

            Texture sheet = new Texture(Gdx.files.internal(path));
            textures.add(sheet);

            TextureRegion[] frames = TextureRegion.split(sheet, frameW, frameH)[0];
            Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
            anim.setPlayMode(name.equals("idle")
                    ? Animation.PlayMode.LOOP
                    : Animation.PlayMode.NORMAL);
            animations.put(name, anim);
        }

        if (!animations.isEmpty()) {
            fallback = animations.containsKey("idle") ? "idle"
                     : animations.keySet().iterator().next();
            current  = fallback;
        }
    }

    // ── ciclo de vida ──────────────────────────────────────────────────────

    public void update(float delta) {
        if (animations.isEmpty()) return;
        stateTime += delta;
        if (playingOnce && resolveAnim(current).isAnimationFinished(stateTime)) {
            current      = fallback;
            stateTime    = 0f;
            playingOnce  = false;
        }
    }

    public void render(SpriteBatch batch, float x, float y, float w, float h) {
        if (animations.isEmpty()) return;
        TextureRegion frame = resolveAnim(current).getKeyFrame(stateTime);
        batch.begin();
        batch.draw(frame, x, y, w, h);
        batch.end();
    }

    // ── control de animaciones ─────────────────────────────────────────────

    /** Cambia a una animación en loop (no interrumpe si ya está activa). */
    public void play(String name) {
        if (!animations.containsKey(name) || current.equals(name)) return;
        current     = name;
        stateTime   = 0f;
        playingOnce = false;
    }

    /** Reproduce una animación una sola vez y vuelve al fallback (idle). */
    public void playOnce(String name) {
        if (!animations.containsKey(name)) return;
        current     = name;
        stateTime   = 0f;
        playingOnce = true;
    }

    public boolean isLoaded()  { return !animations.isEmpty(); }

    public void dispose()      { textures.forEach(Texture::dispose); }

    // ── helpers ────────────────────────────────────────────────────────────

    private Animation<TextureRegion> resolveAnim(String name) {
        return animations.getOrDefault(name, animations.get(fallback));
    }
}
