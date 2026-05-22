package com.rpg.ui.screens;

import com.rpg.ui.RpgGame;
import com.rpg.ui.bridge.CombatController;
import com.rpg.ui.input.PlayerInputAdapter;
import com.rpg.ui.widgets.SaveExitDialog;

public class PostBossScreen extends BaseScreen {

    private final CombatController   controller;
    private final SaveExitDialog     dialog = new SaveExitDialog();
    private final PlayerInputAdapter input  = new PlayerInputAdapter();

    public PostBossScreen(RpgGame game, CombatController controller) {
        super(game);
        this.controller = controller;
        dialog.resetSelection(); // CONTINUE focused por defecto
    }

    @Override
    protected void draw(float delta) {
        if (input.left())    dialog.navigate(-1);
        if (input.right())   dialog.navigate(+1);
        if (input.confirm()) confirm();

        dialog.render(game.batch, game.shapes);
    }

    private void confirm() {
        if (dialog.isSaveExitSelected()) {
            // Navega a la pantalla de slots en modo SAVE con el estado actual
            game.setScreen(new SaveSlotScreen(game, controller));
        } else {
            // Avanza a la siguiente fase manteniendo el estado del jugador (HP, inventario, historia)
            game.setScreen(new CombatScreen(game, controller.nextPhase()));
        }
    }
}
