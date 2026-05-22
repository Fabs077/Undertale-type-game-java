package com.rpg.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.engine.combat.ActionResult;
import com.rpg.ui.RpgGame;
import com.rpg.ui.bridge.CombatController;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BossAssets;
import com.rpg.ui.combat.BossRenderer;
import com.rpg.ui.combat.EclipseAssets;
import com.rpg.ui.combat.KennyBossAssets;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.Soul;
import com.rpg.ui.combat.patterns.CardboxFieldPattern;
import com.rpg.ui.combat.patterns.CoinDropPattern;
import com.rpg.ui.combat.patterns.CoinPattern;
import com.rpg.ui.combat.patterns.CompositeBulletPattern;
import com.rpg.ui.combat.patterns.KnightRatPattern;
import com.rpg.ui.combat.patterns.OrbitalRingPattern;
import com.rpg.ui.combat.patterns.VanTrafficPattern;
import com.rpg.ui.input.PlayerInputAdapter;
import com.rpg.ui.widgets.ActionMenu;
import com.rpg.ui.widgets.ActListWidget;
import com.rpg.ui.widgets.DialogueBox;
import com.rpg.ui.widgets.HpBar;
import com.rpg.ui.widgets.ItemListWidget;
import com.rpg.ui.widgets.SaveExitDialog;

import java.util.HashSet;

public class CombatScreen extends BaseScreen {

    // ── states ─────────────────────────────────────────────────────────────
    private enum State { MENU, DIALOGUE, BULLET_HELL, SUBMENU_ITEM, SUBMENU_ACT, PAUSED }

    // ── layout constants ───────────────────────────────────────────────────
    private static final float SCREEN_W     = 1280f;
    private static final float SCREEN_H     = 720f;
    private static final float ENEMY_ZONE_Y = SCREEN_H * 0.60f;   // 432
    private static final float ACTION_Y     = 8f;
    private static final float ACTION_H     = 72f;
    private static final float HPBAR_Y      = ACTION_Y + ACTION_H + 12f;  // 92
    private static final float BOX_CENTER_X = SCREEN_W / 2f;
    private static final float BOX_CENTER_Y = (ENEMY_ZONE_Y + HPBAR_Y + 28f) / 2f;
    // ── infrastructure ─────────────────────────────────────────────────────
    private final PlayerInputAdapter input = new PlayerInputAdapter();
    private final CombatController   controller;

    // ── widgets ────────────────────────────────────────────────────────────
    private final CombatBox          combatBox;
    private final HpBar              hpBar;
    private final ActionMenu         actionMenu;
    private final DialogueBox        dialogueBox    = new DialogueBox();
    private final ItemListWidget     itemList       = new ItemListWidget();
    private final ActListWidget      actList        = new ActListWidget();
    private final SaveExitDialog     saveExitDialog = new SaveExitDialog();
    private final BossRenderer        bossSprite;

    private State         state = State.MENU;
    private Soul          soul;
    private BulletPattern activePattern;

    // Acción a ejecutar cuando el diálogo activo termina (Z presionado al final)
    private Runnable postDialogueAction = () -> state = State.MENU;

    private static final String[] KENNY_ACT_REFUSALS = {
        "Kenny: ¡Hablar es para los débiles!\n¡Esquiva esto!",
        "Kenny: ¿Negociar? ¡Ni lo sueñes!",
        "Kenny: ¡Ya no hay tiempo para charlas!",
        "Kenny: ¡No me interesa lo que tengas que decir!"
    };
    private int kennyRefusalIdx = 0;

    // Balas que ya golpearon al alma en este bullet hell (evita multi-hit)
    private final HashSet<Bullet> hitBullets = new HashSet<>();

    // Posición y tamaño del sprite del boss en la zona superior
    private static final float BOSS_W = 200f;
    private static final float BOSS_H = 200f;
    private static final float BOSS_X = (SCREEN_W - BOSS_W) / 2f;
    private static final float BOSS_Y = ENEMY_ZONE_Y + (SCREEN_H - ENEMY_ZONE_Y - BOSS_H) / 2f;

    /** Partida nueva. */
    public CombatScreen(RpgGame game) {
        this(game, new CombatController());
    }

    /** Partida cargada desde un save (controller con estado pre-restaurado). */
    public CombatScreen(RpgGame game, CombatController controller) {
        super(game);
        this.controller = controller;
        combatBox  = new CombatBox(BOX_CENTER_X, BOX_CENTER_Y);
        hpBar      = new HpBar(1, controller.getPlayerHp(), controller.getPlayerMaxHp());
        actionMenu = new ActionMenu(ACTION_Y);
        actList.setBossName(controller.getBossName());
        bossSprite = buildBossRenderer(controller.getBossSpriteId());
        showDialogue(controller.getBossIntroDialogue(), () -> state = State.MENU);
    }

    @Override
    public void dispose() {
        bossSprite.dispose();
        if (activePattern != null) activePattern.dispose();
    }

    // ── render loop ────────────────────────────────────────────────────────

    @Override
    protected void draw(float delta) {
        combatBox.update(delta);
        handleInput();
        if (state == State.DIALOGUE)    dialogueBox.update(delta);
        if (state == State.BULLET_HELL) updateBulletHell(delta);

        if (state == State.BULLET_HELL) drawEnemyGrid();
        bossSprite.update(delta);
        bossSprite.render(game.batch, BOSS_X, BOSS_Y, BOSS_W, BOSS_H);

        // Combat box visible except when submenus/dialog cover it completely
        boolean showCombatBox = state == State.MENU
                             || state == State.DIALOGUE
                             || state == State.BULLET_HELL;
        if (showCombatBox) combatBox.render(game.shapes);

        // State-specific content
        switch (state) {
            case MENU -> actionMenu.render(game.shapes, game.batch);

            case DIALOGUE -> {
                dialogueBox.render(
                    game.batch, game.shapes,
                    combatBox.getInnerX(), combatBox.getInnerY(),
                    combatBox.getInnerWidth(), combatBox.getInnerHeight());
                actionMenu.render(game.shapes, game.batch);
            }

            case BULLET_HELL -> renderBulletHell();

            case SUBMENU_ITEM ->
                itemList.render(game.batch, game.shapes,
                    combatBox.getX(), combatBox.getY(),
                    combatBox.getWidth(), combatBox.getHeight());

            case SUBMENU_ACT ->
                actList.render(game.batch, game.shapes,
                    combatBox.getX(), combatBox.getY(),
                    combatBox.getWidth(), combatBox.getHeight());

            default -> { /* PAUSED — handled after HP bar */ }
        }

        // HP bar — drawn before the pause overlay so dialog covers it
        float hpX = (SCREEN_W - hpBar.getTotalWidth()) / 2f;
        hpBar.render(game.shapes, game.batch, hpX, HPBAR_Y);

        // SaveExitDialog renders last so it covers everything with a black overlay
        if (state == State.PAUSED) saveExitDialog.render(game.batch, game.shapes);
    }

    // ── input routing ──────────────────────────────────────────────────────

    private void handleInput() {
        switch (state) {
            case MENU         -> handleMenuInput();
            case DIALOGUE     -> handleDialogueInput();
            case BULLET_HELL  -> handleBulletHellInput();
            case SUBMENU_ITEM -> handleItemSubmenuInput();
            case SUBMENU_ACT  -> handleActSubmenuInput();
            case PAUSED       -> handlePausedInput();
        }
    }

    private void handleMenuInput() {
        if (input.escape()) { openPause(); return; }
        if (input.left())   actionMenu.navigate(-1);
        if (input.right())  actionMenu.navigate(+1);
        if (input.confirm()) executeSelectedAction();
    }

    private void handleDialogueInput() {
        if (input.escape()) { openPause(); return; }
        if (input.confirm()) {
            if (!dialogueBox.isDone()) dialogueBox.skip();
            else postDialogueAction.run();
        }
    }

    private void handleBulletHellInput() {
        // Soul movement is handled inside Soul.update() via Gdx.input.isKeyPressed.
        // No cancel: el jugador debe sobrevivir el turno del enemigo.
    }

    private void handleItemSubmenuInput() {
        if (input.escape()) { openPause(); return; }
        if (input.up())    itemList.navigate(0, +1);
        if (input.down())  itemList.navigate(0, -1);
        if (input.left())  itemList.navigate(-1, 0);
        if (input.right()) itemList.navigate(+1, 0);
        if (input.cancel())  { state = State.MENU; return; }
        if (input.confirm()) {
            ActionResult result = controller.executeItem(itemList.getSelectedIndex());
            showDialogue(result.getMessage(), this::enterBulletHell);
        }
    }

    private void handleActSubmenuInput() {
        if (input.escape()) { openPause(); return; }
        if (input.up())    actList.navigate(0, +1);
        if (input.down())  actList.navigate(0, -1);
        if (input.left())  actList.navigate(-1, 0);
        if (input.right()) actList.navigate(+1, 0);
        if (input.cancel())  { state = State.MENU; return; }
        if (input.confirm()) {
            ActionResult result = controller.executeAct(actList.getSelectedAct().toLowerCase());
            showDialogue(result.getMessage(), this::enterBulletHell);
        }
    }

    private void handlePausedInput() {
        if (input.left())  saveExitDialog.navigate(-1);
        if (input.right()) saveExitDialog.navigate(+1);
        if (input.cancel() || input.escape()) { state = State.MENU; return; }
        if (input.confirm()) {
            if (saveExitDialog.isSaveExitSelected()) {
                game.setScreen(new MainMenuScreen(game));
            } else {
                state = State.MENU;
            }
        }
    }

    // ── state transitions ──────────────────────────────────────────────────

    private void executeSelectedAction() {
        switch (actionMenu.getSelectedIndex()) {
            case 0 -> {
                ActionResult result = controller.executeFight();
                if (result.getDamageDealt() > 0) bossSprite.playOnce("hurt");
                if (result.isCombatEnded()) showDialogue(result.getMessage(), () -> game.goToPostBoss(controller));
                else showDialogue(result.getMessage(), this::enterBulletHell);
            }
            case 1 -> {
                if (controller.isKennyBoss()) {
                    controller.executeAct("hablar"); // advance engine to EnemyBulletHellState
                    String refusal = KENNY_ACT_REFUSALS[kennyRefusalIdx++ % KENNY_ACT_REFUSALS.length];
                    showDialogue(refusal, this::enterBulletHell);
                } else {
                    combatBox.setMode(CombatBox.Mode.NARROW);
                    actList.reset();
                    state = State.SUBMENU_ACT;
                }
            }
            case 2 -> {
                combatBox.setMode(CombatBox.Mode.NARROW);
                itemList.setItems(controller.getInventory());
                state = State.SUBMENU_ITEM;
            }
            case 3 -> {
                ActionResult result = controller.executeMercy();
                if (result.isCombatEnded()) showDialogue(result.getMessage(), () -> game.goToPostBoss(controller));
                else showDialogue(result.getMessage(), this::enterBulletHell);
            }
        }
    }

    private void showDialogue(String message, Runnable onDone) {
        combatBox.setMode(CombatBox.Mode.NARROW);
        dialogueBox.setText(message);
        postDialogueAction = onDone;
        state = State.DIALOGUE;
    }

    private void openPause() {
        saveExitDialog.resetSelection();
        state = State.PAUSED;
    }

    private void enterBulletHell() {
        if (activePattern != null) activePattern.dispose();
        String patternId = controller.getActivePatternId();
        combatBox.setMode(CombatBox.Mode.WIDE);
        if (isGravityPattern(patternId)) combatBox.setPhase2Shape();
        combatBox.snapToTarget();   // soul + pattern must see final dimensions, not lerp start
        hitBullets.clear();
        soul = new Soul(
            combatBox.getInnerX() + combatBox.getInnerWidth()  / 2f,
            combatBox.getInnerY() + combatBox.getInnerHeight() / 2f
        );
        if (isGravityPattern(patternId)) soul.setMode(Soul.SoulMode.BLUE);
        activePattern = createPattern(patternId, soul);
        activePattern.start(combatBox);
        bossSprite.playOnce("attack");
        state = State.BULLET_HELL;
    }

    private static boolean isGravityPattern(String id) {
        return id != null && (id.equals("kenny_van") || id.startsWith("kenny_g"));
    }

    private void exitBulletHell() {
        combatBox.setMode(CombatBox.Mode.NARROW);
        controller.notifyBulletHellComplete();
        if (controller.consumeKennyPhase2Trigger()) {
            showDialogue(
                "Kenny: ¡Ya basta de juegos!\n¡Ahora las REGLAS CAMBIAN!",
                () -> state = State.MENU);
        } else {
            state = State.MENU;
        }
    }

    /** Maps a boss pattern id to the corresponding BulletPattern implementation. */
    private BulletPattern createPattern(String id, Soul soul) {
        return switch (id) {
            case "orbital_ring"  -> new OrbitalRingPattern();
            case "kenny_cardbox" -> new CardboxFieldPattern(soul);
            case "kenny_van"     -> new VanTrafficPattern();
            // No-gravity mixed patterns
            case "kenny_ng2"     -> new CompositeBulletPattern(
                                        new KnightRatPattern(soul),
                                        new CoinPattern());
            case "kenny_ng3"     -> new CompositeBulletPattern(
                                        new KnightRatPattern(soul),
                                        new CoinPattern(),
                                        new CardboxFieldPattern(soul));
            // Gravity mixed patterns (Blue Soul / platformer)
            case "kenny_g2"      -> new CompositeBulletPattern(
                                        new VanTrafficPattern(),
                                        new CoinDropPattern());
            case "kenny_g3"      -> new CompositeBulletPattern(
                                        new VanTrafficPattern(),
                                        new CoinDropPattern(),
                                        new CardboxFieldPattern(soul));
            default              -> new KnightRatPattern(soul);  // "kenny_rat" + fallback
        };
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private void updateBulletHell(float delta) {
        activePattern.update(delta);
        soul.update(delta, combatBox);

        // Una vez que una bala deja de solaparse con el alma se permite que golpee de nuevo.
        // Esto es correcto para balas que orbitan y vuelven a pasar por la misma posición.
        hitBullets.removeIf(b -> !soul.hitbox.overlaps(b.hitbox));

        if (!soul.isInvincible()) {
            for (Bullet b : activePattern.getActiveBullets()) {
                if (!hitBullets.contains(b) && soul.hitbox.overlaps(b.hitbox)) {
                    hitBullets.add(b);
                    soul.onHit();
                    controller.applyBulletHit();
                    hpBar.setHp(controller.getPlayerHp());
                    if (!controller.isPlayerAlive()) {
                        exitBulletHell();
                        game.goToGameOver();
                        return;
                    }
                    break; // i-frames started; remaining bullets pass through this frame
                }
            }
        }

        if (activePattern.isFinished()) exitBulletHell();
    }

    private void renderBulletHell() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        soul.render(game.shapes);
        for (Bullet b : activePattern.getActiveBullets()) b.render(game.shapes);
        activePattern.renderShapes(game.shapes);
        game.shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        activePattern.renderSprites(game.batch);
    }

    /** Elige el renderer correcto según el spriteId del boss activo. */
    private static BossRenderer buildBossRenderer(String spriteId) {
        if ("BossEclipse".equals(spriteId)) return new EclipseAssets(0.18f);
        if ("BossKenny".equals(spriteId)) {
            KennyBossAssets kenny = new KennyBossAssets(0.15f);
            if (kenny.isLoaded()) return kenny;
            kenny.dispose();
        }
        return new BossAssets(spriteId, 64, 64, 0.12f);
    }

    private void drawEnemyGrid() {
        float zoneX = 40f;
        float zoneW = SCREEN_W - 80f;
        float zoneY = ENEMY_ZONE_Y;
        float zoneH = SCREEN_H - ENEMY_ZONE_Y - 4f;
        float cellW = zoneW / 6f;
        float cellH = zoneH / 2f;

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(Color.GREEN);
        for (int col = 0; col <= 6; col++) {
            game.shapes.rect(zoneX + col * cellW, zoneY, 2f, zoneH);
        }
        for (int row = 0; row <= 2; row++) {
            game.shapes.rect(zoneX, zoneY + row * cellH, zoneW, 2f);
        }
        game.shapes.end();
    }

}
