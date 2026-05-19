package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.rpg.ui.RpgGame;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;
import com.rpg.ui.widgets.DialogueBox;

public class DialogueScreen extends BaseScreen {

    private static final float PHASE_Y      = 660f;
    private static final float BOX_CENTER_X = 640f;
    private static final float BOX_CENTER_Y = 330f;

    private final CombatBox   combatBox;
    private final DialogueBox dialogueBox = new DialogueBox();
    private final GlyphLayout phaseLayout;
    private final float       phaseX;

    public DialogueScreen(RpgGame game, int phase) {
        super(game);
        combatBox   = new CombatBox(BOX_CENTER_X, BOX_CENTER_Y);
        String label = "PHASE " + phase;
        phaseLayout  = new GlyphLayout(Fonts.sansLarge, label);
        phaseX       = (1280 - phaseLayout.width) / 2f;
        dialogueBox.setText("Un nuevo retador se acerca...\nPreparate para la Fase " + phase + "!");
    }

    @Override
    protected void draw(float delta) {
        dialogueBox.update(delta);

        if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            if (!dialogueBox.isDone()) {
                dialogueBox.skip();
            } else {
                game.setScreen(new CombatScreen(game));
            }
        }

        game.batch.begin();
        Fonts.sansLarge.setColor(Palette.PRIMARY);
        Fonts.sansLarge.draw(game.batch, phaseLayout, phaseX, PHASE_Y);
        game.batch.end();

        combatBox.render(game.shapes);

        dialogueBox.render(
            game.batch, game.shapes,
            combatBox.getInnerX(), combatBox.getInnerY(),
            combatBox.getInnerWidth(), combatBox.getInnerHeight());
    }
}
